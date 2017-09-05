package org.onosproject.xmpp.driver;


import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import org.dom4j.Document;
import org.dom4j.Element;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;


/**
 * Abstraction of XMPP client.
 */
public abstract class AbstractXmppDevice extends AbstractHandlerBehaviour implements XmppDeviceDriver  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Channel channel;
    protected XmppDeviceId deviceId;
    protected XmppDeviceManager manager;

    @Override
    public void init(XmppDeviceId xmppDeviceId) {
        this.deviceId = xmppDeviceId;
    }

    @Override
    public void setChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
    }

    @Override
    public void setJID(JID jid) {
        this.deviceId.setJID(jid);
    }

    @Override
    public void setManager(XmppDeviceManager manager) {
        if(this.manager==null) {
            this.manager = manager;
        }
    }

    @Override
    public void connectDevice() {
        this.manager.addConnectedDevice(deviceId, this);
    }

    @Override
    public void disconnectDevice() {
        this.channel.close();
        this.manager.removeConnectedDevice(deviceId);
    }

    @Override
    public void sendPacket(Packet packet) {
        Preconditions.checkNotNull(packet);
        if(this.channel.isActive()) {
            this.channel.write(packet);
        } else {
            logger.warn("Dropping XMPP packets for switch {} because channel is not connected: {}",
                    this.deviceId, packet);
        }
    }

    @Override
    public void writeRawXml(Document document) {
        Element root = document.getRootElement();
        Packet packet = null;
        if(root.getName().equals("iq")) {
            packet = new IQ(root);
        }
        else if (root.getName().equals("message")) {
            packet = new Message(root);
        } else if (root.getName().equals("presence")) {
            packet = new Presence(root);
        }
        sendPacket(packet);
    }

    @Override
    public void handlePacket(Packet packet) {
        logger.info("HANDLING PACKET from " + deviceId);
        packet.toString();
    }
}
