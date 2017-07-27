package org.onosproject.xmpp;

/**
 * Created by Tomek Osiński on 17.07.17.
 */
public interface XmppController {


    void addXmppMessageListener(XmppPacketListener listener);

    void removeXmppMessageListener(XmppPacketListener listener);

    void processXmppPacket();

    void writeXmppPacket();


}
