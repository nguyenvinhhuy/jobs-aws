package huynv.jobservice.web.dto;

import java.util.List;

public record CompanyDetailResponse(
    Long id,
    String companyName,
    String address,
    String description,
    String email,
    String logo,
    String phoneNumber,
    Integer recruitmentCount,
    List<RecruitmentSummaryResponse> recruitments
) {
}
