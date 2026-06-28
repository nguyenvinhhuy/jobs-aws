package huynv.jobservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String fullName,
    @NotBlank String address,
    @Email @NotBlank String email,
    @NotBlank String phoneNumber,
    @NotBlank @Size(min = 6, max = 72) String password,
    @NotBlank @Pattern(regexp = "(?i)USER|EMPLOYER") String roleName
) {
}
