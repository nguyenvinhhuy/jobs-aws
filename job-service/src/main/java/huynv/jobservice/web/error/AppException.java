package huynv.jobservice.web.error;

import org.springframework.http.HttpStatus;

public abstract class AppException extends IllegalArgumentException {

    private final HttpStatus status;
    private final String errorCode;

    protected AppException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
