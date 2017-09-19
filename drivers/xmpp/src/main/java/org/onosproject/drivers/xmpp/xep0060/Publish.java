package org.onosproject.drivers.xmpp.xep0060;

import org.onosproject.xmpp.XmppEvent;
import org.xmpp.packet.Packet;

/**
 * Created by autonet on 05.09.17.
 */
public class Publish {

    private Packet packet;

    private String nodeId;

    public Publish(Packet packet) {
        this.packet = packet;
    }


    public String toXML() {
        return packet.toXML();
    }
}
