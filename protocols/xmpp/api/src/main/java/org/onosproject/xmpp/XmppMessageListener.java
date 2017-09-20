package org.onosproject.xmpp;

import org.xmpp.packet.Message;

public interface XmppMessageListener {

    /**
     * Invoke if new event from XMPP device occurs.
     */
    void handleEvent(Message msgEvent);

}
