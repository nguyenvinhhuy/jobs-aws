package huynv.jobservice.web.dto;

public record UserSessionResponse(
    Long id,
    String fullName,
    String email,
    String roleName,
    String image,
    Long companyId
) {
}
