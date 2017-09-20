package org.onosproject.xmpp.ctl.handlers;

import io.netty.channel.Channel;
import org.onosproject.xmpp.stream.StreamError;
import org.onosproject.xmpp.stream.StreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmppStreamHelper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void sendStreamError(Channel channel, StreamError.Condition condition) {
        logger.info("Sending error");
        StreamError error = new StreamError(condition);
        channel.writeAndFlush(error);
    }

}
