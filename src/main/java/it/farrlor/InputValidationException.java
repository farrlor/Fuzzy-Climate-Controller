package it.farrlor;

/**
 * Thrown when a user-supplied value fails validation. The message is written for
 * direct display to the end user.
 */
public class InputValidationException extends RuntimeException {

    public InputValidationException(String message) {
        super(message);
    }
}
