package huynv.jobservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecruitmentUpsertRequest(
    @NotBlank String title,
    @NotBlank String address,
    @NotBlank String description,
    @NotBlank String experience,
    @NotNull Integer quantity,
    @NotBlank String rank,
    @NotBlank String salary,
    @NotBlank String type,
    @NotBlank String deadline,
    @NotNull Long categoryId
) {
}
