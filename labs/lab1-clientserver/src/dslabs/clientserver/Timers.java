package dslabs.clientserver;

import dslabs.framework.Timer;
import dslabs.kvstore.KVStore.KVStoreCommand;
import lombok.Data;

@Data
final class ClientTimer implements Timer {
    static final int CLIENT_RETRY_MILLIS = 100;

    // Your code here...
    private final KVStoreCommand command;
    private final int sequenceNumber;
}
