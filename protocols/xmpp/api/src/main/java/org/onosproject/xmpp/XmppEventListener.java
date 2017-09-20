package org.onosproject.xmpp;

import org.xmpp.packet.Packet;

/**
 * Created by autonet on 14.09.17.
 */
public interface XmppEventListener {


    /**
     * Invoke if new event from XMPP device occurs.
     */
    void handleEvent(Packet event);

}
