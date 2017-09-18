package org.onosproject.drivers.xmpp.xep0060;

import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.driver.AbstractXmppDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

/**
 * Created by autonet on 05.09.17.
 */
public class PubSubXmppDriver extends AbstractXmppDevice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handlePacket(Packet packet) {
        logger.info("HANDLING PACKET from " + packet.getFrom());
        Publish publish = new Publish(packet);
        this.manager.processUpstreamEvent(this.deviceId, publish);
    }



}
