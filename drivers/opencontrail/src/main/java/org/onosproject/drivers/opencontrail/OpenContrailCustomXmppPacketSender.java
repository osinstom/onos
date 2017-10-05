package org.onosproject.drivers.opencontrail;

import com.google.common.base.Preconditions;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.xmpp.*;
import org.xmpp.packet.JID;

public class OpenContrailCustomXmppPacketSender extends AbstractHandlerBehaviour implements CustomXmppPacketSender {

    enum Type {
        IQ, MESSAGE, PRESENCE
    }

    @Override
    public void sendCustomXmppPacket(Type type, Element element) {
        DriverHandler handler = handler();
        XmppController xmppController = handler.get(XmppController.class);
        DeviceId deviceId = handler.data().deviceId();
        Preconditions.checkNotNull(xmppController, "XMPP controller is null");
        JID jid = new JID(deviceId.uri().getSchemeSpecificPart());
        XmppDeviceId xmppDeviceId = new XmppDeviceId(jid);
        XmppDevice xmppDevice = xmppController.getDevice(xmppDeviceId);
        Document document = DocumentFactory.getInstance().createDocument();
        document.setRootElement(element);
        xmppDevice.writeRawXml(document);
    }

}
