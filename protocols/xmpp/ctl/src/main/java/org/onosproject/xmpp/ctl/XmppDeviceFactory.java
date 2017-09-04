package org.onosproject.xmpp.ctl;

import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.driver.AbstractXmppDevice;
import org.onosproject.xmpp.driver.XmppDeviceDriver;
import org.slf4j.Logger;
import org.xmpp.packet.JID;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by autonet on 01.09.17.
 */
public class XmppDeviceFactory {

    private final Logger logger = getLogger(getClass());

    private static XmppDeviceFactory INSTANCE = null;

    public static  XmppDeviceFactory getInstance() {
        if(INSTANCE == null)
            INSTANCE = new XmppDeviceFactory();
        return INSTANCE;
    }

    public XmppDevice getXmppDeviceInstance(JID deviceJid) {

        // temporary solution, TODO: getDriver for device
        XmppDevice device = new AbstractXmppDevice(new XmppDeviceId(deviceJid));

        return device;
    }




}
