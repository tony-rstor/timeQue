package memQueue;

import java.util.Comparator;

/**
 * A comparator for time operations.
 *
 * Perform the subtraction and examination by hand. There's no guarantee of the delta between the timestamps is not
 * a long and we need to insure that we don't overflow an int as a result of any difference. So examine and return the
 * int accordingly.
 *
 * Don't need this now as a comparator of long is quite adequate for the map. However should the sorting criteria
 * get more complex, say it considers the time of first enqueue then we'll need this.
 *
 * @author tonyaiello
 * @version 1.0
 */
public class TimeSorter implements Comparator<TimeEntry> {

    public int compare(TimeEntry t1, TimeEntry t2) {
        long delta = t1.timeWhenAvailable - t2.timeWhenAvailable;
        if (delta < 0)
            return -1;
        else if (delta == 0)
            return 0;
        else return 1;
    }
}
