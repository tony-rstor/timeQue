package com.test;

/**
 * Producer of queue entities.
 *
 * Places a set number of entities on the queue and records the entity id.
 * Sleeps then deletes the entities.
 *
 * @author tonyaiello
 * @version 1.0
 */

import memQueue.MissingQueueIdException;
import memQueue.NoEntityException;
import memQueue.QueueId;
import readResponse.ReadResponse;

/**
 *  Producer test suite.
 *
 *  Produces a set of queue entries under these rules.
 *  Create n entries.
 *  hang out for a bit.
 *  Read all entries with a timeout of 1000 to insure that we can get all entries.
 *  Then dequeue all the entries so that the consumer will certainly run out.
 */
public class Producer implements Runnable {
    public static final int PER_PRODUCER = 10;
    Thread t;
    QueueId pQ;
    byte[] testBuf = new byte[100]; // Use same buffer for all
    String[] entites = new String[PER_PRODUCER];


    public Producer(String workQ) {

        try {
            pQ = QueueId.getQueue(workQ);
        } catch (MissingQueueIdException e) {
            e.printStackTrace();
        }
        t = new Thread(this, "Producer");
        t.start();
    }
    public void run() {
        ReadResponse readResponse = null;

        for (int i = 0; i < PER_PRODUCER; i++) {
            try {
                pQ.enqueue(testBuf);
            } catch (MissingQueueIdException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < PER_PRODUCER; i++) {
            try {
                readResponse = pQ.read(100);
            } catch (NoEntityException e) {
                e.printStackTrace();
            }
            entites[i] = readResponse.queueEntityId;
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Producer interrupted");
        }
        // Now delete them all.
        for (int i = 0; i < PER_PRODUCER; i++) {
            try {
                pQ.dequeue(entites[i]);
            } catch (NoEntityException e) {
                e.printStackTrace();
            }
        }
    }
    public String[] getEntities() {
        return entites;

    }

}
