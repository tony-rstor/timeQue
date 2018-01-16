package memQueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A FIFO based on  time values.
 * This distributes multiple TimeEntries on a sorted map via the time of maturity.
 * Since a sorted map requires unique keys, the value for the map is an array list of those
 * elements that have a common maturity date.
 * <p>
 * Since the time is expressed as a long we need not have another compartor. The natural comparator
 * for a long will be adequate.
 * <p>
 * Adding to this map is looking to see if there's anything at that time value key. If not then a new array
 * list is established and the first element is added. If there is one then we add the new entry as the
 * last entry thereby preserving as much as possible the FIFO nature of the list.
 *
 * @author tonyaiello
 * @version 1.0
 */

public class TimeDistQueue {
    private TreeMap<Long, ArrayList<TimeEntry>> teMap = null;
    private String name;


    public TimeDistQueue(String name) {
        this.name = name;
        teMap = new TreeMap<Long, ArrayList<TimeEntry>>();
    }

    private void addEntry(String entity, long maturityTime) {
        TimeEntry tE = new TimeEntry(entity, maturityTime);
        /*
        If this timeWhenAvailable is already on the map then just add this entry. If not then a new
        key of when this entity is made available is added to the map,
         */
        if (teMap.containsKey(tE.timeWhenAvailable)) {
            ArrayList teList = teMap.get(tE.timeWhenAvailable);
            teList.add(teList.size(), tE);
        }
        /*
        Otherwise we need to create an array list and add it and the maturity date
        to the map.
         */
        else {
            ArrayList teList = new ArrayList<TimeEntry>();
            teList.add(tE);
            teMap.put(tE.timeWhenAvailable, teList);
        }
    }

    public void add(String entity) {
        addEntry(entity, 0);
    }
    public void add(String entity, long timeout) {
        addEntry(entity, timeout );
    }
    /*
    Examine the first key. If it's not less than the current time then there's no
    entry that is mature. In that case throw an exception.
    If there is one, from that list remove the first entry. If that's the only
    entry then delete the time valie as a key.
    Use the timeout value passed in to to then put this entry back on for it to

    To get the entry we'll need an iterator so that we may find the first sorted element
    of the list.
     */
    public TimeEntry getEntryAfter() throws TimeQueueException {
        TimeEntry returnTE = null;

        /*
        If the map is empty then certainly no entry is mature.
         */
        if (teMap.isEmpty()) {
            throw new TimeQueueException();
        }
        /*
        Well there's something on the list. However that's no guarantee that anything is
        mature enough to return. Get the first entry as that's guarateed to have the smallest maturirt
        value. If the maturity date is greater than the current date then we still have nothing to return.
         */
        Map.Entry<Long, ArrayList<TimeEntry>> teList = teMap.firstEntry();
        if (teList.getKey() > System.currentTimeMillis()) {
            throw new TimeQueueException();
        }
        /*
        Ok,so we have something to return. Pull off the first element. If that leaves the
        list empty then we remove the key.
         */
        returnTE = teList.getValue().get(0);
        teList.getValue().remove(0);
        if (teList.getValue().isEmpty()) {
            teMap.remove(teList.getKey());
        }
        return returnTE;
    }
    public void dumpTimeEntries() {
        Iterator teIterator = teMap.entrySet().iterator();

        if (teMap.isEmpty()) {
            System.out.println("Empty map!");
            return;
        }
        while (teIterator.hasNext()) {
            Map.Entry<Long, ArrayList<TimeEntry>> teList = (Map.Entry<Long, ArrayList<TimeEntry>>) teIterator.next();
            System.out.println();
            System.out.println("Key: " + teList.getKey());
            for (TimeEntry te : teList.getValue()) {
                System.out.println(te.queueEntryId);
            }
        }
    }
    public int getSize() {
        return teMap.size();
    }

}

