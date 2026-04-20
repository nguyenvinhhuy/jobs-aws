package huynv.jobservice.service;

import huynv.jobservice.config.MailProperties;
import huynv.jobservice.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpAccountMailService implements AccountMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpAccountMailService.class);

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    public SmtpAccountMailService(JavaMailSender javaMailSender, MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendVerificationEmail(User user, String verificationUrl) {
        if (!mailProperties.isEnabled()) {
            LOGGER.info("Mail delivery disabled. Verification URL for {}: {}", user.getEmail(), verificationUrl);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(mailProperties.getFromAddress(), mailProperties.getFromName());
            helper.setSubject("Verify your Jobs AWS account");
            helper.setText(buildHtml(user, verificationUrl), true);
            javaMailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException exception) {
            throw new IllegalStateException("Unable to send verification email", exception);
        }
    }

    private String buildHtml(User user, String verificationUrl) {
        return """
            <html>
              <body style="font-family:Arial,sans-serif;background:#f5f7fa;padding:24px;color:#1f2937;">
                <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:16px;padding:32px;border:1px solid #e5e7eb;">
                  <p style="font-size:12px;letter-spacing:0.16em;text-transform:uppercase;color:#6b7280;">Jobs AWS</p>
                  <h1 style="margin:0 0 16px;font-size:28px;">Verify your email</h1>
                  <p style="margin:0 0 16px;">Hello %s,</p>
                  <p style="margin:0 0 24px;">Confirm your email address to activate your account and continue using the platform.</p>
                  <a href="%s" style="display:inline-block;background:#14532d;color:#ffffff;text-decoration:none;padding:14px 20px;border-radius:999px;font-weight:600;">Verify account</a>
                  <p style="margin:24px 0 8px;color:#6b7280;">If the button does not work, open this URL:</p>
                  <p style="word-break:break-all;color:#2563eb;">%s</p>
                </div>
              </body>
            </html>
            """.formatted(user.getFullName(), verificationUrl, verificationUrl);
    }
}
