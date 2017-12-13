package org.onosproject.xmpp.ctl.handlers;


import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;
import org.dom4j.*;
import org.onosproject.xmpp.XmppConstants;
import org.onosproject.xmpp.XmppDeviceListener;
import org.onosproject.xmpp.ctl.XmppValidator;
import org.onosproject.xmpp.ctl.exception.UnsupportedStanzaTypeException;
import org.onosproject.xmpp.ctl.exception.XmlRestrictionsException;
import org.onosproject.xmpp.ctl.exception.XmppValidationException;
import org.onosproject.xmpp.stream.StreamClose;
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

    private AsyncXMLStreamReader<AsyncByteArrayFeeder> streamReader = XML_INPUT_FACTORY.createAsyncForByteArray();
    private AsyncByteArrayFeeder streamFeeder = (AsyncByteArrayFeeder) streamReader.getInputFeeder();
    private XmppValidator validator = new XmppValidator();
    private DocumentFactory df = DocumentFactory.getInstance();
    private Element parent;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {

            logger.info("Decoding XMPP data.. ");

            byte[] buffer = new byte[in.readableBytes()];
            in.readBytes(buffer);

            try {
                streamFeeder.feedInput(buffer, 0, buffer.length);
            } catch (XMLStreamException exception) {
                logger.info(exception.getMessage());
                in.skipBytes(in.readableBytes());
                logger.info("Bytes skipped");
            }

            Document document = df.createDocument();

            while (!streamFeeder.needMoreInput()) {
                int type = streamReader.next();
                switch (type) {
                    case AsyncXMLStreamReader.EVENT_INCOMPLETE:
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        QName qname = (streamReader.getPrefix() == null) ?
                                df.createQName(streamReader.getLocalName(), streamReader.getNamespaceURI()) :
                                df.createQName(streamReader.getLocalName(), streamReader.getPrefix(), streamReader.getNamespaceURI());

                        // TODO: Check if new element is IQ, Message or Presence. Ignore otherwise.
                        Element newElement = df.createElement(qname);

                        // add all relevant XML namespaces to Element
                        for (int x = 0; x < streamReader.getNamespaceCount(); x++) {
                            newElement.addNamespace(streamReader.getNamespacePrefix(x), streamReader.getNamespaceURI(x));
                        }
                        // add all attributes to Element
                        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                            newElement.addAttribute(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i));
                        }

                        if(newElement.getName().equals(XmppConstants.STREAM_QNAME)) {
                            StreamOpen streamOpen = new StreamOpen(newElement);
                            validator.validate(streamOpen);
                            out.add(streamOpen);
                            this.reset();
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
                            this.reset();
                            return;
                        }
                        if (parent != null) {
                            if(parent.getParent()!=null)
                                parent = parent.getParent();
                            else {
                                // parent is null, so document parsing is finished, Decoder can return XMPP packet
                                Packet packet = recognizeAndReturnXmppPacket(parent);
                                validate(packet);
                                this.reset();
                                out.add(packet);
                            }
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if(streamReader.hasText()) {
                            if(parent!=null)
                                parent.addText(streamReader.getText());
                        }
                        break;
//                    case XMLStreamConstants.COMMENT:
//                    case XMLStreamConstants.ENTITY_REFERENCE:
//                    case XMLStreamConstants.DTD:
//                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
//                        /**
//                         * From RFC 6120:
//                         * 11.1 XML Restrictions
//                         *  As a result, the following features of XML are prohibited in XMPP:
//
//                         comments (as defined in Section 2.5 of [XML])
//                         processing instructions (Section 2.6 therein)
//                         internal or external DTD subsets (Section 2.8 therein)
//                         internal or external entity references (Section 4.2 therein) with the exception of the predefined entities (Section 4.6 therein)
//
//                         */
////                        throw new XmlRestrictionsException();
                }
            }
        } catch (Exception e) {
            this.reset();
            throw e;
        }

    }

    private void reset() throws XMLStreamException {
//        streamReader.close();
        this.parent = null;
    }

    private void validate(Packet packet) throws UnsupportedStanzaTypeException, XmppValidationException {
        validator.validate(packet);
    }

    private Packet recognizeAndReturnXmppPacket(Element root) throws UnsupportedStanzaTypeException, IllegalArgumentException {
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
