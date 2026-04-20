package huynv.jobservice.web.error;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {

    public BadRequestException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
