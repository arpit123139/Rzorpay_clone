package com.example.razorpay.common.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateResourceException exception){
        ErrorResponse x=ErrorResponse.of(exception.getErrorCode(),exception.getMsg());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(x);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){

        String errorCode=exception.getResourceName().toUpperCase()+"_NOT_FOUND";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInputValidationError(MethodArgumentNotValidException ex){

        List<ApiFieldError> errors =
                ex.getBindingResult().getFieldErrors().stream().map(error->new ApiFieldError(error.getField()
                ,error.getDefaultMessage())).toList();

        String errorCode="BAD_REQUEST";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(errorCode,"Input " +
                "Validation failed ",errors));
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(InvalidStateTransitionException exception){
        String errorCode = "TRANSITION_INVALID";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException exception){
        String errorCode="RATE_LIMIT_EXCEEDED";
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining","0")
                .header("Retry-After",String .valueOf(exception.getReteyAfterSecond()))
                .header("X-RateLimit-Reset",String.valueOf(LocalDateTime.now().plusSeconds(exception.getReteyAfterSecond())))
                .body(ErrorResponse.of(errorCode,exception.getMessage()));

    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflictException(IdempotencyConflictException exception){
        String errorCode = "CONFLICT_REQUEST";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException exception){
        String errorCode = "UNAUTHORIZED";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exception){
        String errorCode = "BAD_REQUEST";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception){
        String errorCode = "SERVER_ERROR";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }
}
