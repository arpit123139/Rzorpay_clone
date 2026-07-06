package com.example.razorpay.common.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalEceptionHandler {

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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> IvalidStateTransistionException(InvalidStateTransitionException exception){
        String errorCode = "TRANSITION_INVALID";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(errorCode,
                exception.getMessage()));

    }
}
