package huynv.jobservice.web.dto;

import java.util.List;

public record RecruitmentDetailResponse(
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
    String companyDescription,
    String companyEmail,
    String companyPhoneNumber,
    Long categoryId,
    String categoryName,
    List<RecruitmentSummaryResponse> relatedRecruitments
) {
}
