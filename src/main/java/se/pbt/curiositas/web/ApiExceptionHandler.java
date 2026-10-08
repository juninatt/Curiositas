package se.pbt.curiositas.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import se.pbt.curiositas.person.PersonNotFoundException;

import java.util.stream.Collectors;

/**
 * Turns errors into Problem Details (RFC 9457), so every error from the API has the same shape.
 * Spring's own errors, such as unreadable JSON or an invalid parameter, are handled by the base
 * class; this class adds the application's own errors.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Answers 404 Not Found when a requested person does not exist.
     *
     * @param exception the error naming the missing id
     * @return the problem description
     */
    @ExceptionHandler
    ProblemDetail handlePersonNotFound(PersonNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * Answers 401 Unauthorized when a write is made without valid credentials. The
     * {@code WWW-Authenticate} header tells the client which authentication scheme to use.
     *
     * @param exception the failed authentication; its details are not revealed to the client
     * @return the problem description with the authentication challenge
     */
    @ExceptionHandler
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Curiositas\"")
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                        "Valid credentials are required to change data"));
    }

    /**
     * Answers 400 Bad Request when a query or path parameter breaks a limit in the contract, such
     * as a negative page number. The generated interfaces validate parameters this way, outside
     * Spring MVC's own validation.
     *
     * @param exception the violated limits
     * @return the problem description, naming each violated limit
     */
    @ExceptionHandler
    ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
