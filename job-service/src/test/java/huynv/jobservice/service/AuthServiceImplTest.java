package huynv.jobservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import huynv.jobservice.web.dto.AuthRequest;
import huynv.jobservice.web.dto.EmailVerificationRequest;
import huynv.jobservice.web.dto.ForgotPasswordRequest;
import huynv.jobservice.web.dto.RegisterRequest;
import huynv.jobservice.web.dto.ResendVerificationRequest;
import huynv.jobservice.web.dto.ResetPasswordRequest;
import huynv.jobservice.web.error.BadRequestException;
import huynv.jobservice.web.error.NotFoundException;
import huynv.jobservice.web.error.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CvRepository cvRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ResetTokenRepository resetTokenRepository;
    @Mock
    private AccountMailService accountMailService;
    @Mock
    private HttpSession session;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MailProperties mailProperties = new MailProperties();
        mailProperties.setFrontendBaseUrl("http://localhost:5173");
        mailProperties.setVerificationTtl(Duration.ofHours(24));
        mailProperties.setPasswordResetTtl(Duration.ofHours(1));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(
            userRepository,
            roleRepository,
            cvRepository,
            companyRepository,
            resetTokenRepository,
            accountMailService,
            mailProperties,
            passwordEncoder
        );
    }

    @Test
    void registerEmployerCreatesCompanyAndVerificationWithoutSession() {
        RegisterRequest request = new RegisterRequest(
            "John Employer",
            "Da Nang",
            "john@example.com",
            "0905000000",
            "12345",
            "EMPLOYER"
        );
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("EMPLOYER");
        Cv cv = new Cv();
        cv.setId(10L);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("EMPLOYER")).thenReturn(Optional.of(role));
        when(cvRepository.save(any(Cv.class))).thenReturn(cv);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(99L);
            }
            return saved;
        });
        when(resetTokenRepository.findByUserIdAndType(99L, "VERIFICATION")).thenReturn(Optional.empty());
        when(resetTokenRepository.save(any(ResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(request);

        assertThat(response.message()).contains("Please verify your email");
        verify(session, never()).setAttribute(any(), any());

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertThat(companyCaptor.getValue().getCompanyName()).isEqualTo("John Employer Company");
        verify(accountMailService).sendVerificationEmail(any(User.class), any(String.class));
    }

    @Test
    void loginRejectsUnverifiedEmail() {
        Role role = new Role();
        role.setRoleName("USER");
        User user = new User();
        user.setId(1L);
        user.setEmail("mary@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole(role);
        user.setEmailVerified(false);

        when(userRepository.findByEmail("mary@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new AuthRequest("mary@example.com", "password"), session))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Email verification required");
    }

    @Test
    void verifyEmailActivatesUserAndRemovesToken() {
        User user = new User();
        user.setId(5L);
        user.setEmail("new@example.com");
        user.setEmailVerified(false);
        user.setStatus(0);

        ResetToken token = new ResetToken();
        token.setId(7L);
        token.setCode(authService.hashToken("plain-token"));
        token.setType("VERIFICATION");
        token.setCreatedAt(Instant.now());
        token.setExpiredTime(Instant.now().plusSeconds(300));
        token.setUser(user);

        when(resetTokenRepository.findByCodeAndType(authService.hashToken("plain-token"), "VERIFICATION")).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.verifyEmail(new EmailVerificationRequest("plain-token"));

        assertThat(response.message()).isEqualTo("Email verified successfully");
        assertThat(user.getEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(1);
        verify(resetTokenRepository).delete(token);
    }

    @Test
    void resendVerificationRequiresExistingUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resendVerification(new ResendVerificationRequest("missing@example.com")))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Account not found");
    }

    @Test
    void forgotPasswordReturnsSameMessageForUnknownEmail() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        var response = authService.forgotPassword(new ForgotPasswordRequest("ghost@example.com"));

        assertThat(response.message()).contains("If this email is registered");
        verify(accountMailService, never()).sendPasswordResetEmail(any(), any());
        verify(resetTokenRepository, never()).save(any());
    }

    @Test
    void forgotPasswordIssuesTokenAndSendsEmailForKnownEmail() {
        User user = new User();
        user.setId(3L);
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resetTokenRepository.findByUserIdAndType(3L, "PASSWORD_RESET")).thenReturn(Optional.empty());
        when(resetTokenRepository.save(any(ResetToken.class))).thenAnswer(i -> i.getArgument(0));

        var response = authService.forgotPassword(new ForgotPasswordRequest("alice@example.com"));

        assertThat(response.message()).contains("If this email is registered");
        verify(resetTokenRepository).save(any(ResetToken.class));
        verify(accountMailService).sendPasswordResetEmail(any(User.class), any(String.class));
    }

    @Test
    void resetPasswordUpdatesPasswordAndDeletesToken() {
        User user = new User();
        user.setId(4L);
        user.setEmail("bob@example.com");

        String plainToken = "my-plain-token";
        ResetToken token = new ResetToken();
        token.setId(9L);
        token.setCode(authService.hashToken(plainToken));
        token.setType("PASSWORD_RESET");
        token.setCreatedAt(Instant.now());
        token.setExpiredTime(Instant.now().plusSeconds(3600));
        token.setUser(user);

        when(resetTokenRepository.findByCodeAndType(authService.hashToken(plainToken), "PASSWORD_RESET"))
            .thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        var response = authService.resetPassword(new ResetPasswordRequest(plainToken, "newpassword123"));

        assertThat(response.message()).contains("Password reset successfully");
        assertThat(user.getPassword()).isNotNull();
        verify(resetTokenRepository).delete(token);
    }

    @Test
    void resetPasswordRejectsInvalidToken() {
        when(resetTokenRepository.findByCodeAndType(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("invalid-token", "newpassword123")))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void resetPasswordRejectsAndDeletesExpiredToken() {
        User user = new User();
        user.setId(6L);

        String plainToken = "expired-token";
        ResetToken token = new ResetToken();
        token.setId(11L);
        token.setCode(authService.hashToken(plainToken));
        token.setType("PASSWORD_RESET");
        token.setCreatedAt(Instant.now().minusSeconds(7200));
        token.setExpiredTime(Instant.now().minusSeconds(3600));
        token.setUser(user);

        when(resetTokenRepository.findByCodeAndType(authService.hashToken(plainToken), "PASSWORD_RESET"))
            .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest(plainToken, "newpassword123")))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("expired");

        verify(resetTokenRepository).delete(token);
    }
}
