package huynv.jobservice.web.dto;

public record ApiMessageResponse(
    String message,
    String verificationUrl
) {
    public ApiMessageResponse(String message) {
        this(message, null);
    }
}
