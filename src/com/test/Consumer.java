package com.test;

/**
 * A Consumer of queue resources.
 *
 * Works off the named queue and runs pulling elements until it encounters an exception. As the delay it puts in
 * is longer than the time to maturity we're certain that getting an exception means the queue has been emptied.
 *
 * @author tonyaiello
 * @version 1.0
 */

import memQueue.MissingQueueIdException;
import memQueue.NoEntityException;
import memQueue.QueueId;
import readResponse.ReadResponse;

public class Consumer implements Runnable {
        Thread t;
        QueueId pQ;
        String lastEntity = "";
        int entitesProcessed = 0;
        ReadResponse readResponse;

        public Consumer(String workQ) {

            try {
                pQ = QueueId.getQueue(workQ);
            } catch (MissingQueueIdException e) {
                e.printStackTrace();
            }
            t = new Thread(this, "Consumer");
            t.start();
        }
        public void run() {
            /*
            The producer needs a bit to get some entries in the queue so just sleep for a bit.
            Then enter a loop where we keep reading events and setting the timeout to 10ms. Then sleep
            for 11 ms and so guarantee that there's at least one entryt on the queue.
            Keep processing til we get an exception as that will only occur by the producer deleting all
            the entries.
             */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Consumer interrupted");
            }
            while (true) {
                try {
                    readResponse = pQ.read(10);
                } catch (NoEntityException e) {
                    break;
                }
                entitesProcessed += 1;
                if (lastEntity == readResponse.queueEntityId) {
                    System.out.println("Error! should not get same element twice");
                }
                lastEntity = readResponse.queueEntityId;
                try {
                    Thread.sleep(11);
                } catch (InterruptedException e) {
                    System.out.println("Consumer interrupted");
                }
            }
        }
        public int getEntitesProcessed() {
            return entitesProcessed;
        }
}
