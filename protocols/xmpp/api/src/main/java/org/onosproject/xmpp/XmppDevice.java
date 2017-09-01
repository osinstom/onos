package org.onosproject.xmpp;

import io.netty.channel.Channel;
import org.xmpp.packet.Packet;



/**
 * Abstracts XMPP device.
 */
public interface XmppDevice {

    /**
     * Sets the associated Netty channel for this device.
     *
     * @param channel the Netty channel
     */
    void setChannel(Channel channel);

    void sendPacket(Packet packet);

    void handlePacket(Packet packet);

}
