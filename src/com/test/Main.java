package com.test;
/**
 * Exercise the classes.
 *
 * @author tonyaiello
 * @version 1.0
 */

import memQueue.*;

import java.util.*;
import java.util.concurrent.TimeoutException;

/*
Test routines for some of the support and client facing functions. Validation of the inner workings on which
the queue is built. Unit tests did some of the validation, this should do the rest.

Two producer and two consumer threads are run without any synchronization. The consumers do wait a second before
starting their consumption just to allow the producers a window to get things on the queue. As the processes are
asynchronous wrt each other we look for success as seeing that all produced entities do make it to the queue.

As each item is on queue until deleted there's no guarantee of order or presence. All we can guarantee is that
consumers see entries and that when deleted they no longer are presented to the consumers. Producers track the
entity ids they put on and remove that many so if the counts didn't add up we'd know there was a synchronization
problem.

 */
public class Main {


    public static void main(String[] args) {

        TimeDistQueue testQueue = new TimeDistQueue(UUID.randomUUID().toString());
        TimeEntry thisOne;

        try {
            thisOne = testQueue.getEntryAfter();

        } catch (TimeQueueException e) {
            System.out.println("Of course, you can't get an entry if there's nothing there.");
        }
        for (int i = 0; i < 10; i++) {
            testQueue.add(UUID.randomUUID().toString());
        }
        for (int i = 0; i < 10; i++) {
            testQueue.add(UUID.randomUUID().toString(), 1);
        }
        for (int i = 0; i < 10; i++) {
            try {
                thisOne = testQueue.getEntryAfter();
            } catch (TimeQueueException e) {
                System.out.print("Huh, no valid entry, fuck!");
                break;
            }
            System.out.println("Removing " + thisOne.getQueueEntryId());
        }
        try {
            Thread.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Slept. Now time is " + System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            try {
                thisOne = testQueue.getEntryAfter();
            } catch (TimeQueueException e) {
                System.out.print("Huh, no valid entry");
                break;
            }
            System.out.println("Removing " + thisOne.getQueueEntryId());
        }
        /*
        Test that queue names can't be duplicated.
         */
        QueueId test1;
        QueueId test2;
        String testName = "Test";
        try {
            test1 = new QueueId(testName);
        }catch (DuplicateQueueIdException e) {
            e.printStackTrace();
        }
        try {
            test2 = new QueueId(testName);
        } catch (DuplicateQueueIdException e) {
            System.out.println("Rightly prohibited creating a second with the same name");
        }
        /*
        Test that a queue may be retrieved by name.
         */
        QueueId getter = null;
        try {
             getter = QueueId.getQueue(testName);
        } catch(MissingQueueIdException e) {
            System.out.println("Failed to get by name, exiting...");
        }
        /*
        Try to get the name to validate.
         */
        if (getter.getQueueName() != testName) {
            System.out.println("Failed to match queue name, exiting...");
        } else {
            System.out.println("Success in getting queue " + getter.getQueueName() + " by name.");
        }
        String pqTest = "pqTest";
        QueueId pqQ = null;
        try {
            pqQ = new QueueId(pqTest);
        } catch (DuplicateQueueIdException e) {
            e.printStackTrace();
        }

        /*
        Concurrency tests
         */
        Producer pThread1 = new Producer(pqTest);
        Producer pThread2 = new Producer(pqTest);
        Consumer cThread1 = new Consumer(pqTest);
        Consumer cThread2 = new Consumer(pqTest);
        try {
            pThread1.t.join();
            pThread2.t.join();
            cThread1.t.join();
            cThread2.t.join();
        } catch(InterruptedException e) {
            System.out.println("Main thread interrupted");
        }
        String[] entites = pThread1.getEntities();
        System.out.println("From producer 1");
        for (int i = 0; i < entites.length; i++){
            System.out.println(i + ") " + entites[i]);
        }
        entites = pThread2.getEntities();
        System.out.println("From producer 2");
        for (int i = 0; i < entites.length; i++){
            System.out.println(i + ") " + entites[i]);
        }
        System.out.println("Max entries on the q is " + pqQ.getMaxEntries());
        int totalProduced = 2 * Producer.PER_PRODUCER;
        if (pqQ.getMaxEntries() != totalProduced) {
            System.out.println("All producer work did NOT get to the queue");
        } else {
            System.out.println("All produced entities did get queued");
        }
        System.out.println("Comsumer1 handled " + cThread1.getEntitesProcessed() + " responses");
        System.out.println("Consumer2 handled " + cThread2.getEntitesProcessed() + " responses");


    }
}
