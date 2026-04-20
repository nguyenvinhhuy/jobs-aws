package huynv.jobservice.web.error;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {

    public ForbiddenException(String errorCode, String message) {
        super(HttpStatus.FORBIDDEN, errorCode, message);
    }
}
