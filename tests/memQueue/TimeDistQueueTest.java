package memQueue;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Some test routines for validating the queue.
 *
 * @author tonyaiello
 * @version 1.0
 */
public class TimeDistQueueTest {
    private TimeDistQueue tdq;

    public TimeDistQueueTest() {
        tdq = new TimeDistQueue(UUID.randomUUID().toString());
    }

    /*
    Add a blast of entries. Just an exerciser.
     */
    @Test
    public void add() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tdq.add(UUID.randomUUID().toString(), i);
                //System.out.println("Adding " + (i * 10 + j));
            }
        }
        tdq.dumpTimeEntries();
    }

    @Test
    public void getEntryAfter() {
        TimeEntry te = null;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++)
                tdq.add(UUID.randomUUID().toString(), i);
        }
        /*
        we should be able to get 10 entries at a time at least.
         */
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                try {
                    te = tdq.getEntryAfter();
                }catch (TimeQueueException e) {
                    System.out.printf("Too soon?");
                }
                System.out.println((i*10+j) + ") " + te.queueEntryId);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*
        This should say that the map is empty.
         */
        tdq.dumpTimeEntries();
        //Verify
        assertEquals(0, tdq.getSize());
    }

}