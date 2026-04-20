package huynv.jobservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplyJobRequest(
    @NotNull Long recruitmentId,
    @NotBlank String text
) {
}
