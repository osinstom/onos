package org.onosproject.xmpp;

/**
 * Created by autonet on 14.09.17.
 */
public interface XmppEventListener {


    /**
     * Invoke if new event from XMPP device occurs.
     */
    void event(XmppEvent event);

}
