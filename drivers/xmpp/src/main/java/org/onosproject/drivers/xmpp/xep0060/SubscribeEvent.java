package org.onosproject.drivers.xmpp.xep0060;

import org.dom4j.Element;
import org.onosproject.xmpp.XmppEvent;

/**
 * Created by autonet on 05.09.17.
 */
public class SubscribeEvent extends XmppEvent {

    private Element element;

    public SubscribeEvent(Element element) {
        this.element = element;
    }

    @Override
    public String toXML() {
        return element.asXML();
    }
}
