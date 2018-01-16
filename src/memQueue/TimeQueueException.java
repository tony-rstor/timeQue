package memQueue;

/**
 * Specific exception for the time queue of entries.
 *
 * Allows the packave to throw an specific exception when there's no valid entry on the queue.
 * @author tonyaiello
 * @version 1.0
 */
public class TimeQueueException extends Exception {
    public TimeQueueException() {}
    public String toString() {
        return "No entries in time queue";
    }
}
