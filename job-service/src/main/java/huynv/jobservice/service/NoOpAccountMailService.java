package huynv.jobservice.service;

import huynv.jobservice.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpAccountMailService implements AccountMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpAccountMailService.class);

    @Override
    public void sendVerificationEmail(User user, String verificationUrl) {
        LOGGER.info("Mail disabled. Verification URL for {}: {}", user.getEmail(), verificationUrl);
    }
}

