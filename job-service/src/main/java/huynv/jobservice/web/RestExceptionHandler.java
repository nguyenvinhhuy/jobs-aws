package huynv.jobservice.web;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import huynv.jobservice.web.error.AppException;
import jakarta.servlet.http.HttpServletRequest;

// Order(-1) ensures this runs before Spring Boot's built-in ProblemDetailsExceptionHandler (order=0)
// so that MethodArgumentNotValidException returns our detailed field-level message instead of
// the generic "Invalid request content."
@Order(-1)
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    private static final String ERROR_BASE_URI = "https://jobs-aws.local/errors/";

    private static URI errorType(String slug) {
        return URI.create(ERROR_BASE_URI + slug);
    }

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        detail.setTitle(exception.getStatus().getReasonPhrase());
        detail.setType(errorType(exception.getErrorCode().toLowerCase()));
        detail.setProperty("errorCode", exception.getErrorCode());
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Validation failed");

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setTitle("Validation failed");
        detail.setType(errorType("validation_failed"));
        detail.setProperty("errorCode", "VALIDATION_FAILED");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleUploadSize(HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Uploaded file is too large");
        detail.setTitle("Invalid upload");
        detail.setType(errorType("upload_too_large"));
        detail.setProperty("errorCode", "UPLOAD_TOO_LARGE");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found: " + request.getRequestURI());
        detail.setTitle("Not Found");
        detail.setType(errorType("not_found"));
        detail.setProperty("errorCode", "NOT_FOUND");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A resource with this data already exists");
        detail.setTitle("Conflict");
        detail.setType(errorType("data_conflict"));
        detail.setProperty("errorCode", "DATA_CONFLICT");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled request failure on {}", request.getRequestURI(), exception);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        detail.setTitle("Internal server error");
        detail.setType(errorType("internal_server_error"));
        detail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }
}
