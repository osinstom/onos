package org.onosproject.xmpp.ctl;

import org.xmpp.packet.Packet;

/**
 * Reader for incoming XMPP packets. Used by Netty XmppDecoder.
 */
public class XmppPacketReader  {

    private static XmppPacketReader INSTANCE;

    public static XmppPacketReader getInstance() {
        if(INSTANCE==null)
            INSTANCE = new XmppPacketReader();
        return INSTANCE;
    }

    public Packet readPacket() {



        return null;
    }

}
