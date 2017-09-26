package org.onosproject.xmpp.ctl.handlers;


import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.evt.EventAllocatorImpl;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import io.netty.util.CharsetUtil;
import org.dom4j.*;
import org.onosproject.xmpp.ctl.exception.UnsupportedStanzaTypeException;
import org.onosproject.xmpp.stream.StreamClose;
import org.onosproject.xmpp.ctl.XmppConstants;
import org.onosproject.xmpp.stream.StreamOpen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decodes an XMPP message for netty pipeline
 */
public class XmppDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AsyncXMLInputFactory XML_INPUT_FACTORY = new InputFactoryImpl();

    AsyncXMLStreamReader<AsyncByteArrayFeeder> streamReader = XML_INPUT_FACTORY.createAsyncForByteArray();
    AsyncByteArrayFeeder streamFeeder = (AsyncByteArrayFeeder) streamReader.getInputFeeder();

    Element parent = null;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        try {

            byte[] buffer = new byte[in.readableBytes()];
            logger.info("Decoding.. {}", new String(buffer, CharsetUtil.UTF_8));

            logger.info("ByteBuf, WriteIdx: {}, ReaderIdx: {}", in.writerIndex(), in.readerIndex());

            in.readBytes(buffer);
            logger.info("ByteBuf, WriteIdx: {}, ReaderIdx: {}", in.writerIndex(), in.readerIndex());

            try {
                streamFeeder.feedInput(buffer, 0, buffer.length);
            } catch (XMLStreamException exception) {
                in.skipBytes(in.readableBytes());
                logger.info("Bytes skipped");
                throw exception;
            }

            DocumentFactory df = DocumentFactory.getInstance();
            Document document = df.createDocument();

            while (!streamFeeder.needMoreInput()) {
                int type = streamReader.next();
                logger.info("Handling TYPE {}", type);
                if(parent!=null)
                    logger.info("Actual parent {}", parent.asXML());
                switch (type) {
                    case AsyncXMLStreamReader.EVENT_INCOMPLETE:
                        break;
                    case XMLStreamConstants.START_ELEMENT:
//                    logger.info("Start element");
                        QName qname = (streamReader.getPrefix() == null) ?
                                df.createQName(streamReader.getLocalName(), streamReader.getNamespaceURI()) :
                                df.createQName(streamReader.getLocalName(), streamReader.getPrefix(), streamReader.getNamespaceURI());

                        // TODO: Check if new element is IQ, Message or Presence. Ignore otherwise.
                        Element newElement = df.createElement(qname);

                        // add all relevant XML namespaces to Element
                        for (int x = 0; x < streamReader.getNamespaceCount(); x++) {
                            logger.info(streamReader.getNamespacePrefix(x) + " " + streamReader.getNamespaceURI(x));
                            newElement.addNamespace(streamReader.getNamespacePrefix(x), streamReader.getNamespaceURI(x));
                        }
                        // add all attributes to Element
                        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                            newElement.addAttribute(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i));
                        }

                        if(newElement.getName().equals(XmppConstants.STREAM_QNAME)) {
                            out.add(new StreamOpen(newElement));
                            return;
                        }

                        if (parent != null) {
                            parent.add(newElement);
                        } else {
                            document.add(newElement);
                        }
                        parent = newElement;

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if(streamReader.getLocalName().equals(XmppConstants.STREAM_QNAME)) {
                            // return Stream Error
                            out.add(new StreamClose());
                            return;
                        }
                        if (parent != null) {
                            if(parent.getParent()!=null)
                                parent = parent.getParent();
                            else {
                                // parent is null, so document parsing is finished, Decoder can return XMPP packet
                                out.add(recognizeAndReturnXmppPacket(parent));
                                parent = null;
                            }
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if(streamReader.hasText()) {
                            if(parent!=null)
                                parent.addText(streamReader.getText());
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw e;
        }

    }

    private Packet recognizeAndReturnXmppPacket(Element root) throws UnsupportedStanzaTypeException {
        checkNotNull(root);
        Packet packet = null;
        if(root.getName().equals(XmppConstants.IQ_QNAME)) {
            packet = new IQ(root);
        } else if (root.getName().equals(XmppConstants.MESSAGE_QNAME)) {
            packet = new Message(root);
        } else if (root.getName().equals(XmppConstants.PRESENCE_QNAME)) {
            packet = new Presence(root);
        } else {
            throw new UnsupportedStanzaTypeException("Unrecognized XMPP Packet");
        }
        return packet;
    }

}
