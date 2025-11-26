package com.fraudengine.exception;

/**
 * Simple custom runtime exceptions for JWT handling to allow GlobalExceptionHandler
 * to map them uniformly.
 */
public class JwtExceptions {
    public static class MissingSecretException extends RuntimeException {
        public MissingSecretException(String msg) { super(msg); }
    }
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String msg) { super(msg); }
    }
    public static class ExpiredTokenException extends RuntimeException {
        public ExpiredTokenException(String msg) { super(msg); }
    }
}

