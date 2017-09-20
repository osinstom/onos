package org.onosproject.xmpp;

import org.xmpp.packet.Presence;

public interface XmppPresenceListener {

    /**
     * Invoke if new event from XMPP device occurs.
     */
    void handleEvent(Presence presenceEvent);

}
