package huynv.jobservice.web;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.springframework.core.annotation.Order;

import huynv.jobservice.web.error.AppException;
import jakarta.servlet.http.HttpServletRequest;

// Order(-1) ensures this runs before Spring Boot's built-in ProblemDetailsExceptionHandler (order=0)
// so that MethodArgumentNotValidException returns our detailed field-level message instead of
// the generic "Invalid request content."
@Order(-1)
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        detail.setTitle(exception.getStatus().getReasonPhrase());
        detail.setType(URI.create("https://jobs-aws.local/errors/" + exception.getErrorCode().toLowerCase()));
        detail.setProperty("errorCode", exception.getErrorCode());
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Validation failed");

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setTitle("Validation failed");
        detail.setType(URI.create("https://jobs-aws.local/errors/validation_failed"));
        detail.setProperty("errorCode", "VALIDATION_FAILED");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleUploadSize(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Uploaded file is too large");
        detail.setTitle("Invalid upload");
        detail.setType(URI.create("https://jobs-aws.local/errors/upload_too_large"));
        detail.setProperty("errorCode", "UPLOAD_TOO_LARGE");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found: " + request.getRequestURI());
        detail.setTitle("Not Found");
        detail.setType(URI.create("https://jobs-aws.local/errors/not_found"));
        detail.setProperty("errorCode", "NOT_FOUND");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled request failure on {}", request.getRequestURI(), exception);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        detail.setTitle("Internal server error");
        detail.setType(URI.create("https://jobs-aws.local/errors/internal_server_error"));
        detail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }
}
