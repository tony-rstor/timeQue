package memQueue;

/**
 * An exception for when a queue is empty or has no mature entity.
 *
 * @author tonyaiello
 * @version 1.0
 */
public class MissingQueueIdException extends Exception {

    public MissingQueueIdException() {

    }
    public String toString() {
        return "Missing Queue Id";
    }
}
