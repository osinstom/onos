package org.onosproject.xmpp;

import org.onlab.util.Identifier;
import org.xmpp.packet.JID;

import java.net.InetSocketAddress;

/**
 * The class representing a network device identifier.
 * This class is immutable.
 */
public final class XmppDeviceId extends Identifier<JID> {

    public XmppDeviceId(JID jid) {
        super(jid);
    }

}
