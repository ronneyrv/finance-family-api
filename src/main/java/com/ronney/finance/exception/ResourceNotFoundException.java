package com.ronney.finance.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(
            String message
    ) {
        super(message);
    }
}
