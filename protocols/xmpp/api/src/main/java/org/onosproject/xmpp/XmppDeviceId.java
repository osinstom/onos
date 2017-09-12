package org.onosproject.xmpp;

import org.onlab.util.Identifier;
import org.xmpp.packet.JID;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.util.Tools.toHex;

/**
 * The class representing a network device identifier.
 * This class is immutable.
 */
public final class XmppDeviceId extends Identifier<String> {

    private static final String SCHEME = "xmpp";

    private JID jid = null;

    public XmppDeviceId(InetSocketAddress address) {
        super("xmpp" + address);
    }

    public void setJID(JID jid) {
        this.jid = jid;
    }

    @Override
    public String toString() {
        return jid == null ? identifier.toString() : identifier.toString() + "/" + jid.toString();
    }

    public static URI uri(XmppDeviceId xmppDeviceId) {
        try {
            return new URI(SCHEME, xmppDeviceId.toString(), null);
        } catch (URISyntaxException e) {
            return null;
        }
    }


}
