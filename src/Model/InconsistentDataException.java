package Model;

/**
 * This exception class deals with game states that contradict themselves.
 */
public class InconsistentDataException extends ClueException {

    public InconsistentDataException(String message) {
        super("Inconsistent Data: " + message);
    }

}
