package huynv.jobservice.web.dto;

public record RecruitmentSummaryResponse(
    Long id,
    String title,
    String address,
    String description,
    String salary,
    String type,
    String rank,
    String experience,
    Integer quantity,
    String deadline,
    Integer view,
    String createdAt,
    Long companyId,
    String companyName,
    Long categoryId,
    String categoryName
) {
}
