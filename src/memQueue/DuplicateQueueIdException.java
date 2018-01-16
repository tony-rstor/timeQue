package memQueue;
/**
 * Specific exception for the id queue of entries.
 * <p>
 * Allows the packave to throw an specific exception the name is duplicated.
 *
 * @author tonyaiello
 * @version 1.0
 */
public class DuplicateQueueIdException extends Exception{

    public DuplicateQueueIdException() {
    }

    public String toString() {
        return "Duplicate Queue Id";
    }
}

