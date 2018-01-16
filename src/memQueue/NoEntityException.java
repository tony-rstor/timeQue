package memQueue;

/**
 * An exception for when no valid entry on a queue is found.
 *
 * @author tonyaiello
 * @version 1.0
 */
public class NoEntityException extends Exception {
    public NoEntityException() {

    }

    @Override
    public String toString() {
        return "No valid entity";
    }
}
