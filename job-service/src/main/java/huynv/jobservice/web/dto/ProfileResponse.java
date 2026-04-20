package huynv.jobservice.web.dto;

public record ProfileResponse(
    Long id,
    String fullName,
    String address,
    String email,
    String description,
    String image,
    String phoneNumber,
    Integer status,
    String roleName,
    String cvFileName,
    CompanySummaryResponse company
) {
}
