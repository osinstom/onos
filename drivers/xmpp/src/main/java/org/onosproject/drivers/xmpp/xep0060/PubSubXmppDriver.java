package org.onosproject.drivers.xmpp.xep0060;

import org.dom4j.Element;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.XmppEvent;
import org.onosproject.xmpp.driver.AbstractXmppDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.util.List;

import static org.onosproject.xmpp.XmppDeviceId.uri;

/**
 * Created by autonet on 05.09.17.
 */
public class PubSubXmppDriver extends AbstractXmppDevice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String PUBSUB_NS = "http://jabber.org/protocol/pubsub";
    private final String PUBSUB_ELEMENT = "pubsub";

    enum Method {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

    @Override
    public void handlePacket(Packet packet) {
        logger.info("HANDLING PACKET from " + packet.getFrom());

        if(isPubSub(packet)) {
            Element pubSubElement = packet.getElement().element(PUBSUB_ELEMENT);
            handlePubSub(packet.getElement().element(PUBSUB_ELEMENT));
        }
    }

    private boolean isPubSub(Packet packet) {
        Element element = packet.getElement().element(PUBSUB_ELEMENT);
        return element != null && element.getNamespace() != null && element.getNamespace().getURI().equals(PUBSUB_NS);
    }

    private void handlePubSub(Element element) {
        Method method = getMethod(element);
        switch(method) {
            case SUBSCRIBE:
                SubscribeEvent event = new SubscribeEvent(DeviceId.deviceId(uri(this.deviceId)), element);
                notifyEvent(event);
        }
    }

    private Method getMethod(Element element) {
        Element methodElement = getChildElement(element);
        String name = methodElement.getName();
        logger.info("Parsed 'name' {}", name);
        switch(name) {
            case "subscribe":
                return Method.SUBSCRIBE;
            case "unsubscribe":
                return Method.UNSUBSCRIBE;
            case "publish":
                return Method.PUBLISH;
            case "retract":
                return Method.RETRACT;
        }

        return null;
    }

    private Element getChildElement(Element element) {
        Element child = (Element) element.elements().get(0); // the first element is related to pubsub operation
        logger.info(child.asXML());
        return child;
    }


    private void notifyEvent(XmppEvent event) {
        this.manager.processUpstreamEvent(this.deviceId, event);
    }


}
