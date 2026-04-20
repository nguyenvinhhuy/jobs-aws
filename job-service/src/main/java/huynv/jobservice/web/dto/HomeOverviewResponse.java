package huynv.jobservice.web.dto;

import java.util.List;

public record HomeOverviewResponse(
    long totalCompany,
    long totalRecruitment,
    long totalCandidate,
    List<CategorySummaryResponse> topCategories,
    List<CompanySummaryResponse> topCompanies,
    List<RecruitmentSummaryResponse> topRecruitments
) {
}
