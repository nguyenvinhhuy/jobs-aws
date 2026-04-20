package huynv.jobservice.service;

import huynv.jobservice.domain.User;

public interface AccountMailService {

    void sendVerificationEmail(User user, String verificationUrl);
}
