package com.example.distributed_razorpay.common_lib.exceptions;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
