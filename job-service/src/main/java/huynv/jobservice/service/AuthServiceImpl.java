package huynv.jobservice.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import huynv.jobservice.config.MailProperties;
import huynv.jobservice.domain.Company;
import huynv.jobservice.domain.Cv;
import huynv.jobservice.domain.ResetToken;
import huynv.jobservice.domain.Role;
import huynv.jobservice.domain.User;
import huynv.jobservice.repository.CompanyRepository;
import huynv.jobservice.repository.CvRepository;
import huynv.jobservice.repository.ResetTokenRepository;
import huynv.jobservice.repository.RoleRepository;
import huynv.jobservice.repository.UserRepository;
import huynv.jobservice.web.dto.ApiMessageResponse;
import huynv.jobservice.web.dto.AuthRequest;
import huynv.jobservice.web.dto.EmailVerificationRequest;
import huynv.jobservice.web.dto.ForgotPasswordRequest;
import huynv.jobservice.web.dto.RegisterRequest;
import huynv.jobservice.web.dto.ResendVerificationRequest;
import huynv.jobservice.web.dto.ResetPasswordRequest;
import huynv.jobservice.web.dto.UserSessionResponse;
import huynv.jobservice.web.error.ConflictException;
import huynv.jobservice.web.error.NotFoundException;
import huynv.jobservice.web.error.UnauthorizedException;
import huynv.jobservice.web.error.BadRequestException;
import jakarta.servlet.http.HttpSession;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static final String SESSION_USER_ID = "CURRENT_USER_ID";

    private static final String TOKEN_VERIFICATION = "VERIFICATION";
    private static final String TOKEN_PASSWORD_RESET = "PASSWORD_RESET";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CvRepository cvRepository;
    private final CompanyRepository companyRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final AccountMailService accountMailService;
    private final MailProperties mailProperties;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
        UserRepository userRepository,
        RoleRepository roleRepository,
        CvRepository cvRepository,
        CompanyRepository companyRepository,
        ResetTokenRepository resetTokenRepository,
        AccountMailService accountMailService,
        MailProperties mailProperties,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cvRepository = cvRepository;
        this.companyRepository = companyRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.accountMailService = accountMailService;
        this.mailProperties = mailProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ApiMessageResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Email already exists");
        });

        Role role = roleRepository.findByRoleName(request.roleName().toUpperCase(Locale.ROOT))
            .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found"));

        Cv cv = cvRepository.save(new Cv());

        User user = new User();
        user.setFullName(request.fullName());
        user.setAddress(request.address());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(0);
        user.setEmailVerified(false);
        user.setEmailVerifiedAt(null);
        user.setRole(role);
        user.setCv(cv);

        User savedUser = userRepository.save(user);
        if ("EMPLOYER".equals(role.getRoleName())) {
            Company company = new Company();
            company.setCompanyName(request.fullName() + " Company");
            company.setAddress(request.address());
            company.setEmail(request.email());
            company.setPhoneNumber(request.phoneNumber());
            company.setDescription("New employer company profile");
            company.setStatus(1);
            company.setUser(savedUser);
            companyRepository.save(company);
        }

        String verificationUrl = issueVerification(savedUser);
        return verificationResponse("Registration successful. Please verify your email before logging in.", verificationUrl);
    }

    @Override
    public UserSessionResponse login(AuthRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Invalid email or password");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new UnauthorizedException("EMAIL_NOT_VERIFIED", "Email verification required");
        }

        session.setAttribute(SESSION_USER_ID, user.getId());
        return toSessionResponse(user);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public ApiMessageResponse verifyEmail(EmailVerificationRequest request) {
        ResetToken token = resetTokenRepository.findByCodeAndType(hashToken(request.token()), TOKEN_VERIFICATION)
            .orElseThrow(() -> new NotFoundException("VERIFICATION_TOKEN_INVALID", "Verification token is invalid"));

        if (token.getExpiredTime().isBefore(Instant.now())) {
            resetTokenRepository.delete(token);
            throw new BadRequestException("VERIFICATION_TOKEN_EXPIRED", "Verification token has expired");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        user.setStatus(1);
        userRepository.save(user);
        resetTokenRepository.delete(token);

        return new ApiMessageResponse("Email verified successfully");
    }

    @Override
    @Transactional
    public ApiMessageResponse resendVerification(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "Account not found"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return new ApiMessageResponse("Email is already verified");
        }

        String verificationUrl = issueVerification(user);
        return verificationResponse("Verification email sent", verificationUrl);
    }

    @Override
    @Transactional
    public ApiMessageResponse forgotPassword(ForgotPasswordRequest request) {
        String resetUrl = null;
        Optional<User> userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isPresent()) {
            resetUrl = issuePasswordReset(userOpt.get());
            try {
                accountMailService.sendPasswordResetEmail(userOpt.get(), resetUrl);
            } catch (Exception e) {
                // Log but do not propagate — preserves anti-enumeration guarantee
                LOGGER.warn("Failed to send password reset email to {}", request.email(), e);
            }
        }
        String msg = "If this email is registered, a password reset link has been sent";
        return !mailProperties.isEnabled() && resetUrl != null
            ? new ApiMessageResponse(msg, resetUrl)
            : new ApiMessageResponse(msg);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public ApiMessageResponse resetPassword(ResetPasswordRequest request) {
        ResetToken token = resetTokenRepository.findByCodeAndType(hashToken(request.token()), TOKEN_PASSWORD_RESET)
            .orElseThrow(() -> new BadRequestException("RESET_TOKEN_INVALID", "Reset token is invalid or expired"));

        if (token.getExpiredTime().isBefore(Instant.now())) {
            resetTokenRepository.delete(token);
            throw new BadRequestException("RESET_TOKEN_EXPIRED", "Reset token has expired");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetTokenRepository.delete(token);

        return new ApiMessageResponse("Password reset successfully. You can now sign in with your new password.");
    }

    @Override
    @Transactional
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Override
    public User requireCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Long id)) {
            throw new UnauthorizedException("AUTHENTICATION_REQUIRED", "You must login first");
        }
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));
    }

    @Override
    public UserSessionResponse getCurrentSession(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Long id)) {
            return null;
        }
        return userRepository.findById(id).map(this::toSessionResponse).orElse(null);
    }

    private String issueToken(User user, String type, Duration ttl) {
        String plainToken = generateToken();
        Instant now = Instant.now();

        ResetToken token = resetTokenRepository.findByUserIdAndType(user.getId(), type).orElseGet(ResetToken::new);
        token.setUser(user);
        token.setType(type);
        token.setCode(hashToken(plainToken));
        token.setCreatedAt(now);
        token.setExpiredTime(now.plus(ttl));
        resetTokenRepository.save(token);

        return plainToken;
    }

    private String issueVerification(User user) {
        String plainToken = issueToken(user, TOKEN_VERIFICATION, mailProperties.getVerificationTtl());
        String verificationUrl = buildUrl("/verify-email", plainToken);
        accountMailService.sendVerificationEmail(user, verificationUrl);
        return verificationUrl;
    }

    private String issuePasswordReset(User user) {
        String plainToken = issueToken(user, TOKEN_PASSWORD_RESET, mailProperties.getPasswordResetTtl());
        return buildUrl("/reset-password", plainToken);
    }

    private ApiMessageResponse verificationResponse(String message, String verificationUrl) {
        return mailProperties.isEnabled()
            ? new ApiMessageResponse(message)
            : new ApiMessageResponse(message, verificationUrl);
    }

    private String buildUrl(String path, String token) {
        String baseUrl = Optional.ofNullable(mailProperties.getFrontendBaseUrl())
            .filter(url -> !url.isBlank())
            .orElse("http://localhost:5173");
        return baseUrl + path + "?token=" + token;
    }

    private String generateToken() {
        byte[] random = new byte[32];
        SECURE_RANDOM.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private UserSessionResponse toSessionResponse(User user) {
        Long companyId = companyRepository.findByUserId(user.getId()).map(Company::getId).orElse(null);
        return new UserSessionResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().getRoleName(),
            user.getImage(),
            companyId
        );
    }
}
