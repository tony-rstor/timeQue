package memQueue;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import readResponse.ReadResponse;



/**
 * A synchronized queue for handling multiple producers and consumers.
 *
 * Each entry is characterized by a unique id.
 * Each entry has a maturity date associated with the id. This information exist on the TimeDistQueue.
 * Each entry is kept in a hash map where the key is the unique id and the value is the byte stream for it.
 *
 * So we have this relationship:
 * TimeDistQueue                EntityMap
 * EntityId<----------------->EntityId
 * Maturity time              Object
 *
 * The method of management is this:
 * When the value is added a unique key is created. The k,v pair is added to the map.  The id and a maturity time
 * of 'now' is added to the time queue.
 * As reads are done, each read removes the id from timequeue. The id is checked for existence in the hash map.
 * If the entry is not found then the id was deleted. That's ok. However, it means another entry must be pulled from
 * the TimeDistQueue. The process is repeated until either a mature id that *does* exist is found or we run out of mature
 * entries.
 * A successful read(one that does find an map entry by the id) will add back the entry on the TimeDistQueue with some
 * delta value that will have it mature later. This is to mean that an entry is processed only once. A successful
 * operation should include a deletion of the entry prior to the timeout expiring. Should the timeout expire the
 * implication is that the processing party failed to complete the task and so another must now take it up.
 *
 * An unsuccessful one will throw an exception. This means that clients DO NOT WAIT. If there's nothing here then
 * the exception tells them to move along and check back later.
 *
 * An entry may be deleted at any time. If so, it will later encounter exactly the conditions described above. It will
 * exist on the TimeDistQueue but be missing from the map.
 *
 * The TimeDistQueue is a tree map where the key is the maturity time and the value is the list of all entities sharing
 * that maturity time. This allows the map to be sorted in ascending order w.r.t. time and to keep the list of those
 * entities sharing a maturity time in FIFO order(add at the end of each list.) It would be simpler if we can just
 * use the maturity time but a tree map requires unique keys so that doesn't work when there's no restriction on entities
 * sharing a time to maturity.
 *
 * Notes:
 * Some changes from the spec were done.
 * The QueueId was to be a string. However that then requires keeping some translation of the string
 * to a reference. Rather than do that, I changed the QueueId to be the class and the id became an object reference.
 *
 * The create and get functions didn't have a way to communicate failure, duplicate or missing name respectively, so
 * I added exceptions they could throw to communicate errors.
 *
 * This necessitated using a static function for the class for the getQueue operation. The method is a class
 * method that returns a reference to a QueueId instead of a string.
 *
 * The ReadResponse has all members public. Didn't feel the need for getters and setters on such a public class,
 * however with any complexity proper encapsulation *would* be worthwhile.
 *
 * Since the read function didn't have any means of communicating failure I added an exception to it to show that
 * an entity was not available.
 *
 * Testing:
 * I've not used JUnit before and may not be using it properly here. The JUnit tests I wrote worked and exercised
 * the class but not everything fit under that framework. So the test and validation is split between the JUnit work
 * and work in a main function.
 *
 *
 * @author tonyaiello
 * @version 1.0
 *
 */

public class QueueId {
    static HashMap<String, QueueId> queMaps = null;
    static Semaphore qMapsMutex;

    static {
        queMaps = new HashMap<String, QueueId>();
        qMapsMutex = new Semaphore(1);
    }

    private String queueName;
    private TimeDistQueue tq = null;
    private Semaphore qMutex;
    private boolean queueDisabled;
    private HashMap<String, byte[]> qEntityMap;
    private int maxEntries = 0;

    /**
     * Construct a new queue instance.
     * Since queue names must be unique this will throw an exception should it be that the name
     * given matches an instance already in existence.
     * @param queueName a unique string to name the queue
     * @throws DuplicateQueueIdException
     */
    public QueueId(String queueName) throws DuplicateQueueIdException {
        try {
            qMapsMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (queMaps.containsKey(queueName)) {
            qMapsMutex.release();
            throw new DuplicateQueueIdException();

        }
        this.queueName = queueName;
        queMaps.put(queueName, this);
        qMapsMutex.release();
        /*
        We can release protection now as the rest deals with instance data.
         */
        tq = new TimeDistQueue(UUID.randomUUID().toString());
        qMutex = new Semaphore(1);
        qEntityMap = new HashMap<String, byte[]>();
    }

    /**
     * Retrieve an instance by name.
     * Since names are unique if there's no match then an exception is thrown.
     * As the queMaps is a common resource be sure to acquire exclusive access to it.
     * @param queueName must match an existing queue.
     * @return an instance if a queue matching the name is found.
     * @throws MissingQueueIdException
     */
    public static QueueId getQueue(String queueName) throws MissingQueueIdException {
        try {
            qMapsMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!queMaps.containsKey(queueName)) {
            qMapsMutex.release();
            throw new MissingQueueIdException();
        }
        /*
        Queues don't get deleted so we can release the mutex and be certain that returning the value is safe.
         */
        qMapsMutex.release();
        return queMaps.get(queueName);
    }
    public String getQueueName() {
        return queueName;
    }

    /**
     * Deleting a queue may only be done if there's no work items in it. If that's not true then the queue
     * may not be deleted. Deleting consists of removing it from the list of queues that may be done.
     * Since the check is the map is empty it is known then that the time queue must be empty as there can only
     * be entries on the time queue if there are entries in the map.
     */

    public boolean deleteQueue(){
        try{
            qMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (qEntityMap.isEmpty() == false) {
            qMutex.release();
            return false;
        }
        queueDisabled = true;
        try {
            qMapsMutex.acquire();
        } catch(Exception e) {
            e.printStackTrace();
        }
        queMaps.remove(queueName);
        qMapsMutex.release();
        qMutex.release();
        return true;
    }

    /**
     * To the queue add an entry.
     *
     * Note that adding depends on the queue being enabled. If so, for an object, a byte array, create a
     * unique id and add it the name space for this queue. Place the name on the time queue with the current time
     * so that it's immediately mature.
     * @param object a byte array reference
     * @throws MissingQueueIdException
     */
    public void enqueue(byte[] object) throws MissingQueueIdException {
        String entityId = UUID.randomUUID().toString();
        try {
            qMutex.acquire();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (queueDisabled) {
            qMutex.release();
            throw new MissingQueueIdException();
        }
        qEntityMap.put(entityId, object);
        if (qEntityMap.size() > maxEntries) {
            maxEntries = qEntityMap.size();
        }
        tq.add(entityId);
        qMutex.release();
    }

    /**
     * From the queue the entry identified by the entity id is removed.
     * If the queue is disabled or the entity is not found then an exception is thrown.
     * @param entityId
     * @throws NoEntityException
     */
    public void dequeue(String entityId) throws NoEntityException {
        try {
            qMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (queueDisabled) {
            qMutex.release();
            throw new NoEntityException();
        }
        if (qEntityMap.containsKey(entityId) == false) {
            qMutex.release();
            throw new NoEntityException();
        }
        qEntityMap.remove(entityId);
        qMutex.release();
    }

    /**
     * From the queue get a mature entry and reset its time to maturity.
     * Since this is to return a ReadResponse we need to communicate when there is no mature entry
     * on the list. Should this be true then a NoEntity exception is thrown to inform the client of that.
     *
     * In handling this note that getting an entry from the time queue is not sufficient. Things on the time
     * queue must be qualified against the map with the entity id. Only then can it be known that the entity is
     * present. As part of this the entity is requeued to the time queue when found.
     *
     * @param timeout milliseconds to add to the maturity date
     * @return a ReadResponse with the object and reference information.
     * @throws NoEntityException
     */
    public ReadResponse read(long timeout) throws NoEntityException {
        TimeEntry timeResponse = null;
        ReadResponse readResponse = null;
        try {
            qMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (readResponse == null) {
            try {
                timeResponse = tq.getEntryAfter();

            } catch (TimeQueueException e) {
                qMutex.release();
                throw new NoEntityException();
            }
            if (qEntityMap.containsKey(timeResponse.queueEntryId)) {
                readResponse = new ReadResponse(this, timeResponse.queueEntryId,
                        qEntityMap.get(timeResponse.queueEntryId));
            }
        }
        /*
        Before we leave here we have to put the entity back on the time queue with the new timeout value.
        We must have dequeued an entry as we'd have thrown an exception if there was nothing to return.
         */
        tq.add(timeResponse.queueEntryId, timeout);
        qMutex.release();
        return readResponse;
    }
    /*
    Instrumentation...
     */
    public int getMaxEntries() {
        return maxEntries;
    }
}
