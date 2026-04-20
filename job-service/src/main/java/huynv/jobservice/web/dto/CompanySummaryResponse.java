package huynv.jobservice.web.dto;

public record CompanySummaryResponse(
    Long id,
    String companyName,
    String address,
    String description,
    String email,
    String logo,
    String phoneNumber,
    Integer recruitmentCount
) {
}
