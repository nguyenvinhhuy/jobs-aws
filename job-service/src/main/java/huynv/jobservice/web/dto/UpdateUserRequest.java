package huynv.jobservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    @NotBlank String fullName,
    @NotBlank String address,
    @Email @NotBlank String email,
    String description,
    @NotBlank String phoneNumber
) {
}
