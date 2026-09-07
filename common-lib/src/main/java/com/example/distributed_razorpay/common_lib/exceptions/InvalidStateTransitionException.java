package com.example.distributed_razorpay.common_lib.exceptions;

import lombok.Getter;

@Getter
public class InvalidStateTransitionException extends RuntimeException {

    private final String fromState;

    private final String toState;

    public InvalidStateTransitionException(String fromState,String toState){
        super("Invalid Transition from" +fromState +"to "+ toState);
        this.fromState=fromState;
        this.toState=toState;
    }
}
