package org.onosproject.xmpp.ctl.handlers;

import io.netty.channel.ChannelHandlerContext;
import org.dom4j.Element;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.ctl.stream.StreamClose;
import org.onosproject.xmpp.ctl.stream.StreamEvent;
import org.onosproject.xmpp.ctl.stream.StreamOpen;
import org.onosproject.xmpp.ctl.XmppDeviceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.StreamError;

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
        if(streamEvent instanceof StreamOpen) {
            openStream((StreamOpen) streamEvent);
        }
        if(streamEvent instanceof StreamClose) {
            closeStream();
        }
        if(streamEvent instanceof StreamError) {
            handleStreamError((StreamError) streamEvent);
        }
    }

    private void closeStream() {
        // get XMPP device by remoteAddress and close stream
        logger.info("Closing stream with");
        SocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.info("Closing stream with {}", addr);
        writeStreamClose();
    }

    private void writeStreamClose() {
        this.ctx.channel().writeAndFlush(new StreamClose());
    }

    private void openStream(StreamOpen streamOpen) {
        logger.info(streamOpen.toXML());

        // 1. respond with StreamOpen
        writeStreamOpen(streamOpen);

        // 2. set interal state of stream to OPEN
        registerXmppDevice(streamOpen);
    }

    private void registerXmppDevice(StreamOpen streamOpen) {
        if(this.ctx.channel().isActive()) { // check if channel is still active
            XmppDeviceFactory factory = XmppDeviceFactory.getInstance();
            XmppDevice device = factory.getXmppDeviceInstance((InetSocketAddress) ctx.channel().remoteAddress());
            device.setJID(streamOpen.getFromJID());
            device.setChannel(ctx.channel());
            device.connectDevice();
        }

    }

    private void writeStreamOpen(StreamOpen streamOpenFromDevice) {
        Element element = streamOpenFromDevice.getElement().createCopy();
        JID from = streamOpenFromDevice.getFromJID();
        JID to = streamOpenFromDevice.getToJID();
        element.addAttribute("from", to.toString());
        element.addAttribute("to", from.toString());
        element.addAttribute("id", this.ctx.channel().id().asShortText()); // use Netty Channel ID as XMPP stream ID

        StreamOpen streamOpenToDevice = new StreamOpen(element);
        this.ctx.channel().writeAndFlush(streamOpenToDevice);
    }

    private void handleStreamError(StreamError streamEvent) {
        // TODO: Handling XMPP Stream Error
    }
}
