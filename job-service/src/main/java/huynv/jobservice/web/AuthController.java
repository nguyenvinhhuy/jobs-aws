package huynv.jobservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import huynv.jobservice.service.AuthService;
import huynv.jobservice.web.dto.ApiMessageResponse;
import huynv.jobservice.web.dto.AuthRequest;
import huynv.jobservice.web.dto.EmailVerificationRequest;
import huynv.jobservice.web.dto.RegisterRequest;
import huynv.jobservice.web.dto.ResendVerificationRequest;
import huynv.jobservice.web.dto.UserSessionResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiMessageResponse register(@Validated @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public UserSessionResponse login(@Validated @RequestBody AuthRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @PostMapping("/logout")
    public ApiMessageResponse logout(HttpSession session) {
        authService.logout(session);
        return new ApiMessageResponse("Logged out");
    }

    @GetMapping("/me")
    public UserSessionResponse me(HttpSession session) {
        return authService.getCurrentSession(session);
    }

    @PostMapping("/verification/confirm")
    public ApiMessageResponse confirmVerification(@Validated @RequestBody EmailVerificationRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/verification/resend")
    public ApiMessageResponse resendVerification(@Validated @RequestBody ResendVerificationRequest request) {
        return authService.resendVerification(request);
    }
}
