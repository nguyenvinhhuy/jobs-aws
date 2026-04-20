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
import huynv.jobservice.web.dto.RegisterRequest;
import huynv.jobservice.web.dto.ResendVerificationRequest;
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
        authService = new AuthServiceImpl(
            userRepository,
            roleRepository,
            cvRepository,
            companyRepository,
            resetTokenRepository,
            accountMailService,
            mailProperties
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
        when(resetTokenRepository.findByUserId(99L)).thenReturn(Optional.empty());
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
            .isInstanceOf(IllegalArgumentException.class)
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
        token.setCreatedAt(Instant.now());
        token.setExpiredTime(Instant.now().plusSeconds(300));
        token.setUser(user);

        when(resetTokenRepository.findByCode(authService.hashToken("plain-token"))).thenReturn(Optional.of(token));
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
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Account not found");
    }
}
