package com.example.distributed_razorpay.common_lib.exceptions;

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
