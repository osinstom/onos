package org.onosproject.xmpp;

import org.xmpp.packet.IQ;

public interface XmppIqListener {

    /**
     * Invoke if new event from XMPP device occurs.
     */
    void handleEvent(IQ iqEvent);

}
