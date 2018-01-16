package readResponse;

import memQueue.QueueId;

/**
 * ReadResponse is the common structure for passing information between the packages.
 *
 * The fields are made public so getters and setters are not necessary.
 *
 * @author tonyaiello
 * @version 1.0
 */
public class ReadResponse {
    public QueueId queueId;
    public String queueEntityId;
    public byte[] object;

    public ReadResponse(QueueId qId, String qEntryId, byte[] entryObject) {
        queueId = qId;
        queueEntityId = qEntryId;
        object = entryObject;
    }
}

