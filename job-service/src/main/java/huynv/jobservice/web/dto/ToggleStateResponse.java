package huynv.jobservice.web.dto;

public record ToggleStateResponse(
    boolean active,
    String message
) {
}
