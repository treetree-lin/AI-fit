package org.lin.fitnesscommon.exception;

import org.springframework.http.HttpStatus;

/**
 * @author lin
 * @date 2026-04-17
 */

public class CustomException  extends RuntimeException{
    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
