package org.onosproject.xmpp.ctl;

import org.onosproject.xmpp.ctl.exception.XmppValidationException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by autonet on 07.10.17.
 */
public class XmppValidator {

    public void validateIQ(IQ iq) throws XmppValidationException{
        try {
            validateIqHeader(iq);
            validateJID(iq.getFrom());
            validateJID(iq.getTo());
        } catch(Exception e) {
            throw new XmppValidationException();
        }
    }

    public void validateMessage(Message message) {

    }

    public void validatePresence(Presence presence) {

    }

    private void validateIqHeader(IQ iq) {

    }

    public void validateJID(JID jid) {
        checkNotNull(jid);
    }
}
