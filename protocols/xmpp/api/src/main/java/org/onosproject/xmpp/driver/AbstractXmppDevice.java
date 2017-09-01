package org.onosproject.xmpp.driver;


import io.netty.channel.Channel;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.xmpp.XmppDeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;


/**
 * Abstraction of XMPP client.
 */
public class AbstractXmppDevice extends AbstractHandlerBehaviour implements XmppDeviceDriver  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Channel channel;
    private XmppDeviceId deviceId;

    public AbstractXmppDevice(XmppDeviceId deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void setChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
    }

    @Override
    public void sendPacket(Packet packet) {

    }

    @Override
    public void handlePacket(Packet packet) {
        logger.info("HANDLING PACKET from " + deviceId);
    }
}
