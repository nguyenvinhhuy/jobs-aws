package huynv.jobservice.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private boolean enabled;
    private String fromAddress;
    private String fromName;
    private String frontendBaseUrl;
    private Duration verificationTtl = Duration.ofHours(24);
    private Duration passwordResetTtl = Duration.ofHours(1);
}
