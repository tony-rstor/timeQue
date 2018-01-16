package memQueue;

/**
 * Simple tests of the QueueId class
 *
 * Does some basic testing, single threaded. Synchronization testing requires multiple threads and is handled
 * in the main.
 *
 * @author tonyaiello
 * @version 1.0
 */

import org.junit.Assert;
import org.junit.Test;
import readResponse.ReadResponse;

import static org.junit.Assert.*;

public class QueueIdTest {
    private QueueId aTestQ, bTestQ, cTestQ;
    private String aName = "atest", bName = "btest", cName = "ctest";
    // byte[] testBuf = new byte[100];

    /*
    A constructor for the test class to create a few queues for later testing.
     */
    public QueueIdTest() {

        try {
            aTestQ = new QueueId(aName);
        } catch (DuplicateQueueIdException e) {
            e.printStackTrace();
        }

        try {
            bTestQ = new QueueId(bName);
        } catch (DuplicateQueueIdException e) {
            e.printStackTrace();
        }
        try {
            cTestQ = new QueueId(cName);
        } catch (DuplicateQueueIdException e) {
            e.printStackTrace();
        }
    }
    /*
    Since the queues are sticky in the namespace each test routine can't run in sequence because
    they reuse the constructor. So either remove the constructor or have a clean up to remove
    the queues. let's have the clean up.
     */
    private void cleanup() {
        if (!aTestQ.deleteQueue()) {
            System.out.println("Failed to delete queue A");
        }
        if (!bTestQ.deleteQueue()) {
            System.out.println("Failed to delete queue B");
        }
        if (!cTestQ.deleteQueue()) {
            System.out.println("Failed to delete queue C");
        }

    }
    /*
    Since we have static information each test can't necessarily run idependently. So
    here we remove all the queue instances to make it possible.
     */

    @Test
    public void getQueue() {
        QueueId getter = null;
        try {
            getter = QueueId.getQueue(aName);
        } catch (MissingQueueIdException e) {
            e.printStackTrace();
        }
        assertEquals(getter.getQueueName(), aTestQ.getQueueName());
        System.out.println("getQueue good");
        cleanup();
    }

    @Test
    public void getQueueName() {
        assertEquals(aTestQ.getQueueName(), aName);
        assertEquals(bTestQ.getQueueName(), bName);
        assertEquals(cTestQ.getQueueName(), cName);
        System.out.println("getQueueName good");
        cleanup();
    }

    @Test
    public void deleteQueue() {
        QueueId deleter = null;
        String deleteQueue = "delete";
        try {
            deleter = new QueueId(deleteQueue);
        } catch (DuplicateQueueIdException e) {
            System.out.println("Problem here...");
            e.printStackTrace();
        }
        deleter.deleteQueue();
        QueueId shouldBeGone = null;
        try {
            shouldBeGone = QueueId.getQueue(deleteQueue);
        } catch (MissingQueueIdException e) {
            assertEquals(shouldBeGone, null);
            System.out.println("Queue gone.");
        }
        cleanup();
    }

    /*
    The enquue and dequeue functions work the read function so there's no explicit unit test
    as the other tests cannot function without that doing it's job.
     */

    /*
    Enqueue a single buffer. Validate that it was enqueud by issuing a read.
     */
    @Test
    public void enqueue() {
        String testMsg = "A test message";
        byte[] testBuf = testMsg.getBytes();

        try {
            aTestQ.enqueue(testBuf);
        } catch (MissingQueueIdException e) {
            e.printStackTrace();
        }
        ReadResponse readResponse = null;
        try {
            readResponse = aTestQ.read(0);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        String responseString = new String(readResponse.object);
        assertEquals(testMsg, responseString);
        /*
        Test that the same object is still on the queue as it should be
         */
        ReadResponse readResponse2 = null;
        try {
            readResponse2 = aTestQ.read(0);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        assertEquals(readResponse2.queueEntityId, readResponse.queueEntityId);
        /*
        Test that if we set the maturity out and try an immediat read we get nothing.
        Read and set the delay out, then sleep and try again expecting success
         */
        try {
            readResponse2 = aTestQ.read(1000);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        try {
            readResponse2 = aTestQ.read(0);
        } catch (NoEntityException e) {
            System.out.println("As expected, no mature entry");
        }
        try {
            Thread.sleep(1000);
        }catch(Exception e) {
            e.printStackTrace();
        }
        try {
            readResponse2 = aTestQ.read(0);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        try {
            aTestQ.dequeue(readResponse.queueEntityId);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        assertEquals(readResponse2.queueEntityId, readResponse.queueEntityId);
        cleanup();
    }

    @Test
    public void dequeue() {
        /*
        Dequeue relies on enqueue to have an entry to work with.
         */
        String testMsg = "A test message";
        byte[] testBuf = testMsg.getBytes();

        try {
            aTestQ.enqueue(testBuf);
        } catch (MissingQueueIdException e) {
            e.printStackTrace();
        }
        ReadResponse dequeueResponse = null;
        try {
            dequeueResponse = aTestQ.read(0);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        try {
            aTestQ.dequeue(dequeueResponse.queueEntityId);
        } catch (NoEntityException e) {
            e.printStackTrace();
        }
        /*
        This should have an exception thrown as the queue is empty
         */
        try {
            dequeueResponse = aTestQ.read(0);
        } catch(NoEntityException e) {
            System.out.println("Queue is now properly empty");
        }
        cleanup();
    }

}