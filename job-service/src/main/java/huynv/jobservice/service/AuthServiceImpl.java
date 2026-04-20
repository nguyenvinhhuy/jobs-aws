package huynv.jobservice.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import huynv.jobservice.web.dto.RegisterRequest;
import huynv.jobservice.web.dto.ResendVerificationRequest;
import huynv.jobservice.web.dto.UserSessionResponse;
import huynv.jobservice.web.error.ConflictException;
import huynv.jobservice.web.error.NotFoundException;
import huynv.jobservice.web.error.UnauthorizedException;
import huynv.jobservice.web.error.BadRequestException;
import jakarta.servlet.http.HttpSession;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    static final String SESSION_USER_ID = "CURRENT_USER_ID";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CvRepository cvRepository;
    private final CompanyRepository companyRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final AccountMailService accountMailService;
    private final MailProperties mailProperties;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(
        UserRepository userRepository,
        RoleRepository roleRepository,
        CvRepository cvRepository,
        CompanyRepository companyRepository,
        ResetTokenRepository resetTokenRepository,
        AccountMailService accountMailService,
        MailProperties mailProperties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cvRepository = cvRepository;
        this.companyRepository = companyRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.accountMailService = accountMailService;
        this.mailProperties = mailProperties;
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
    @Transactional
    public ApiMessageResponse verifyEmail(EmailVerificationRequest request) {
        ResetToken token = resetTokenRepository.findByCode(hashToken(request.token()))
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

    private String issueVerification(User user) {
        String plainToken = generateToken();
        String hashedToken = hashToken(plainToken);
        Instant now = Instant.now();

        ResetToken token = resetTokenRepository.findByUserId(user.getId()).orElseGet(ResetToken::new);
        token.setUser(user);
        token.setCode(hashedToken);
        token.setCreatedAt(now);
        token.setExpiredTime(now.plus(mailProperties.getVerificationTtl()));
        resetTokenRepository.save(token);

        String verificationUrl = buildVerificationUrl(plainToken);
        accountMailService.sendVerificationEmail(user, verificationUrl);
        return verificationUrl;
    }

    private ApiMessageResponse verificationResponse(String message, String verificationUrl) {
        return mailProperties.isEnabled()
            ? new ApiMessageResponse(message)
            : new ApiMessageResponse(message, verificationUrl);
    }

    private String buildVerificationUrl(String token) {
        String baseUrl = Optional.ofNullable(mailProperties.getFrontendBaseUrl())
            .filter(url -> !url.isBlank())
            .orElse("http://localhost:5173");
        return baseUrl + "/verify-email?token=" + token;
    }

    private String generateToken() {
        byte[] random = new byte[32];
        new java.security.SecureRandom().nextBytes(random);
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
