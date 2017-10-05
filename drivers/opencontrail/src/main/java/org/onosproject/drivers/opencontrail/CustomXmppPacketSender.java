package org.onosproject.drivers.opencontrail;

import org.dom4j.Element;
import org.onosproject.net.driver.HandlerBehaviour;

public interface CustomXmppPacketSender extends HandlerBehaviour {

    void sendCustomXmppPacket(OpenContrailCustomXmppPacketSender.Type type, Element element);

}
