package com.example.razorpay.common.exceptions;

import lombok.Getter;

@Getter
public class BuisnessRuleViolationException extends RuntimeException{
    private final String errorCode;
    private final String msg;
    public BuisnessRuleViolationException(String errorCode, String msg) {
        super(msg);
        this.msg=msg;
        this.errorCode = errorCode;

    }
}
