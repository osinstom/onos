package org.onosproject.xmpp.ctl.stream;

/**
 * Created by autonet on 12.09.17.
 */
public class StreamClose implements StreamEvent {

    @Override
    public String toXML() {
        return "</stream:stream>";
    }
}
