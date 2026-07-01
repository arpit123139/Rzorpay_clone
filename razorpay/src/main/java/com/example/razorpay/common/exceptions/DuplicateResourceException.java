package com.example.razorpay.common.exceptions;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException{

    private final String errorCode;
    private final String msg;
    public DuplicateResourceException(String errorCode, String msg) {
        super(msg);
        this.msg=msg;
        this.errorCode = errorCode;

    }
}
