package huynv.jobservice.service;

import huynv.jobservice.domain.User;
import huynv.jobservice.web.dto.ApiMessageResponse;
import huynv.jobservice.web.dto.AuthRequest;
import huynv.jobservice.web.dto.EmailVerificationRequest;
import huynv.jobservice.web.dto.RegisterRequest;
import huynv.jobservice.web.dto.ResendVerificationRequest;
import huynv.jobservice.web.dto.UserSessionResponse;
import jakarta.servlet.http.HttpSession;

public interface AuthService {

    ApiMessageResponse register(RegisterRequest request);

    UserSessionResponse login(AuthRequest request, HttpSession session);

    ApiMessageResponse verifyEmail(EmailVerificationRequest request);

    ApiMessageResponse resendVerification(ResendVerificationRequest request);

    void logout(HttpSession session);

    User requireCurrentUser(HttpSession session);

    UserSessionResponse getCurrentSession(HttpSession session);
}
