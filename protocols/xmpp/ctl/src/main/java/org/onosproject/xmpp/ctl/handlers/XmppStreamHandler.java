package org.onosproject.xmpp.ctl.handlers;

import io.netty.channel.ChannelHandlerContext;
import org.dom4j.Element;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.stream.StreamClose;
import org.onosproject.xmpp.stream.StreamError;
import org.onosproject.xmpp.stream.StreamEvent;
import org.onosproject.xmpp.stream.StreamOpen;
import org.onosproject.xmpp.ctl.XmppDeviceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;


import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by autonet on 13.09.17.
 */
public final class XmppStreamHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ChannelHandlerContext ctx;
    protected final StreamEvent streamEvent; // represents XML element related to XMPP stream

    public XmppStreamHandler(ChannelHandlerContext ctx, StreamEvent streamEvent) {
        this.ctx = ctx;
        this.streamEvent = streamEvent;
    }

    @Override
    public void run(){
        XmppDeviceFactory factory = XmppDeviceFactory.getInstance();

        if(streamEvent instanceof StreamOpen) {
            StreamOpen streamOpen = (StreamOpen) streamEvent;
            XmppDevice device = factory.getXmppDeviceInstanceByJid(streamOpen.getFromJID());
//            device.setJID(streamOpen.getFromJID());
            device.setChannel(ctx.channel());
            device.openStream(streamOpen);
            device.connectDevice();
        }
        if(streamEvent instanceof StreamClose) {
            XmppDevice device = factory.getXmppDeviceInstanceBySocketAddress((InetSocketAddress) ctx.channel().remoteAddress());
            device.closeStream();
        }
        if(streamEvent instanceof StreamError) {
            XmppDevice device = factory.getXmppDeviceInstanceBySocketAddress((InetSocketAddress) ctx.channel().remoteAddress());
            device.handleStreamError((StreamError) streamEvent);
        }
    }
}
