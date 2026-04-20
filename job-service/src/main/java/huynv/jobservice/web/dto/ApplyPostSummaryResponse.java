package huynv.jobservice.web.dto;

public record ApplyPostSummaryResponse(
    Long id,
    String candidateName,
    String candidateEmail,
    String cvFileName,
    String text,
    Integer status,
    String createdAt,
    Long recruitmentId,
    String recruitmentTitle
) {
}
