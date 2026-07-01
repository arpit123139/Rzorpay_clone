package com.example.razorpay.common.exceptions;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class ErrorResponse{



    String errorCode;
    String errorDescription;
    LocalDateTime timeStamp;
    List<ApiFieldError> fieldError;


    public static ErrorResponse of(String errorCode,String errorDescription){
        log.info("Inside Error Response");
        log.info(errorCode);
        log.info(errorDescription);
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),null);
    }

    public static ErrorResponse of(String errorCode,String errorDescription,List<ApiFieldError>fieldError){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),fieldError);
    }

}
@Getter
@AllArgsConstructor
@Setter
class ApiFieldError{
    String field;
    String msg;
}


