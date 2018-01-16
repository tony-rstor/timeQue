package memQueue;

/**
 * Entries in the list of UU-identified objects.
 * <p>
 * Each entry has the UUID of the object and the time at which it will be available for use.
 *
 * @author tonyaiello
 * @version 1.0
 */

public class TimeEntry {
    String queueEntryId;
    long timeWhenAvailable;


    /**
     * Create an entry for this UUID, setting the time of availability to some later date.
     * <p>
     * This adds the timeout value to the current time to create the time in the future when the object
     * will be made available. Note that the timeout must be expressed in milliseconds as well. The timeout
     * may be zero which means the object is IMMEDIATELY available.
     *
     * @param queueUUID the unique id of the object.
     * @param timeout   time in milliseconds to wait for the object to be made available.
     */
    public TimeEntry(String queueUUID, long timeout) {
        queueEntryId = queueUUID;
        timeWhenAvailable = System.currentTimeMillis() + timeout;
    }

    public String getQueueEntryId() {
        return queueEntryId;
    }
}
