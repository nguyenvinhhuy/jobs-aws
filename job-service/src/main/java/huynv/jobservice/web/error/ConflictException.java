package huynv.jobservice.web.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
