package com.transporter.assignment.exception;

/**
 * Thrown when the database has no lanes or quotes and assignment cannot proceed.
 */
public class InsufficientDataException extends RuntimeException {

    public InsufficientDataException(String message) {
        super(message);
    }
}
