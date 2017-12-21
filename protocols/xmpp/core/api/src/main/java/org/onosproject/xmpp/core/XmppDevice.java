package org.onosproject.xmpp.core;

import io.netty.channel.Channel;
import org.dom4j.Document;
import org.onosproject.xmpp.core.driver.XmppDeviceManager;
import org.onosproject.xmpp.core.stream.StreamOpen;
import org.onosproject.xmpp.core.stream.StreamError;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;


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

    Channel getChannel();

    void setManager(XmppDeviceManager manager);

    void connectDevice();

    void disconnectDevice();

    void sendPacket(Packet packet);

    /**
     * Method for sending raw XML data as XMPP packet.
     *
     * @param document the XML data
     */
    void writeRawXml(Document document);

    void handlePacket(Packet packet);

    void init(XmppDeviceId xmppDeviceId);

    void closeStream();

    void openStream(StreamOpen streamOpen);

    void handleStreamError(StreamError streamError);

    void sendStreamError(StreamError.Condition streamErrorCondition);

    void sendError(PacketError packetError);
}
