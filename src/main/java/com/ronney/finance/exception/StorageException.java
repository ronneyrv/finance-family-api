package com.ronney.finance.exception;

import org.springframework.http.HttpStatus;

public class StorageException extends BusinessException {

    public StorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}