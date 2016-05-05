package com.axway.apigwgcm.util;

/**
 * Created by su on 10/11/2014.
 */
public class ValidationException extends RuntimeException {

    public ValidationException() {
    }

    public ValidationException(String detailMessage) {
        super(detailMessage);
    }

    public ValidationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ValidationException(Throwable throwable) {
        super(throwable);
    }
}
