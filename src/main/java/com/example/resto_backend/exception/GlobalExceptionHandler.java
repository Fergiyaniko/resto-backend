package com.example.resto_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE = "https://api.example.com/problems";

    private static final String ACCESS_DENIED = BASE + "/access-denied";
    private static final String AUTHENTICATION_REQUIRED = BASE + "/authentication-required";
    private static final String DUPLICATE_RESOURCE = BASE + "/duplicate-resource";
    private static final String INTERNAL_SERVER_ERROR = BASE + "/internal-server-error";
    private static final String INVALID_CREDENTIALS = BASE + "/invalid-credentials";
    private static final String NOT_FOUND = BASE + "/not-found";
    private static final String VALIDATION_ERROR = BASE + "/validation-error";

    /* -------------------- 400 BAD REQUEST -------------------- */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(VALIDATION_ERROR));
        return problem;
    }

    /* -------------------- 401 UNAUTHORIZED -------------------- */

    /**
     * Thrown when authentication is missing or invalid
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleUnauthorized(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail("Authentication is required");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(AUTHENTICATION_REQUIRED));
        return problem;
    }

    @ExceptionHandler(InvalidUsernameOrPasswordException.class)
    public ProblemDetail handleInvalidCredentials(
            InvalidUsernameOrPasswordException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Unauthorized");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(INVALID_CREDENTIALS));
        return problem;
    }

    /* -------------------- 403 FORBIDDEN (optional but recommended) -------------------- */

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleForbidden(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("You do not have permission to access this resource");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(ACCESS_DENIED));
        return problem;
    }

    /* -------------------- 404 NOT FOUND -------------------- */

    /**
     * Triggered when no endpoint matches the request
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail resourceNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Not Found");
        problem.setDetail("The requested resource was not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(NOT_FOUND));
        return problem;
    }

    /* -------------------- 409 CONFLICT -------------------- */

    /**
     * Triggered when database conflict happens
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConstraintViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Duplicate resource");
        problem.setDetail("Username or email already exists");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(DUPLICATE_RESOURCE));
        return problem;
    }

    /* -------------------- FALLBACK (optional but useful) -------------------- */

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setType(URI.create(INTERNAL_SERVER_ERROR));
        return problem;
    }
}
