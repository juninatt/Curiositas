package se.pbt.curiositas.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
