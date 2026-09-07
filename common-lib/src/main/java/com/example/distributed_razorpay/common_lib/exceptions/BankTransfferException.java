package com.example.distributed_razorpay.common_lib.exceptions;


public class BankTransfferException extends RuntimeException {

    private final String errorCode;
    private final String msg;
    public BankTransfferException(String errorCode, String msg) {
        super(msg);
        this.msg=msg;
        this.errorCode = errorCode;

    }
}
