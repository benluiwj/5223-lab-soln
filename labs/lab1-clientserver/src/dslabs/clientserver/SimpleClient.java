package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore.KVStoreCommand;
import dslabs.kvstore.KVStore.KVStoreResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static dslabs.clientserver.ClientTimer.CLIENT_RETRY_MILLIS;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * See the documentation of {@link Client} and {@link Node} for important
 * implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
    private final Address serverAddress;

    // Your code here...

    private KVStoreCommand kvStoreCommand = null;
    private KVStoreResult kvStoreResult = null;
    private int sequenceNumber = 0;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        // Your code here...
        if (!(command instanceof KVStoreCommand)) {
            throw new IllegalArgumentException();
        }

        KVStoreCommand cmd = (KVStoreCommand) command;
        this.kvStoreCommand = cmd;
        this.kvStoreResult = null;

        send(new Request(cmd, sequenceNumber), serverAddress);
        set(new ClientTimer(cmd, sequenceNumber), CLIENT_RETRY_MILLIS);
        sequenceNumber++;
    }

    @Override
    public synchronized boolean hasResult() {
        // Your code here...
        return kvStoreResult != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        // Your code here...
        while (!hasResult()) {
            wait();
        }
        return kvStoreResult;
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        // Your code here...
        if (m.result() instanceof KVStoreResult &&
                m.sequenceNum() <= sequenceNumber) {
            kvStoreResult = (KVStoreResult) m.result();
            //            kvStoreResult.set(m.sequenceNum(), (KVStoreResult) m.result());
            notify();
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        // Your code here...
        if (Objects.equals(t.command(), kvStoreCommand) &&
                kvStoreResult == null) {
            send(new Request(kvStoreCommand,
                    t.sequenceNumber()), serverAddress);
            set(t, CLIENT_RETRY_MILLIS);
        }
    }
}
