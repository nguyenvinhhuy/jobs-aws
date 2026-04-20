package huynv.jobservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String fullName,
    @NotBlank String address,
    @Email @NotBlank String email,
    @NotBlank String phoneNumber,
    @NotBlank String password,
    @NotBlank String roleName
) {
}
