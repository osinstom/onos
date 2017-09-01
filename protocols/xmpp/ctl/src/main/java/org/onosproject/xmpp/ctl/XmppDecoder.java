package org.onosproject.xmpp.ctl;


import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.evt.EventAllocatorImpl;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes an XMPP message for netty pipeline
 */
public class XmppDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AsyncXMLInputFactory XML_INPUT_FACTORY = new InputFactoryImpl();

    private final AsyncXMLStreamReader streamReader = XML_INPUT_FACTORY.createAsyncForByteArray();
    private final AsyncByteArrayFeeder streamFeeder = (AsyncByteArrayFeeder) streamReader.getInputFeeder();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        logger.info("Decoding..");
        byte[] buffer = new byte[in.readableBytes()];
        in.readBytes(buffer);
        try {
            streamFeeder.feedInput(buffer, 0, buffer.length);
        } catch (XMLStreamException exception) {
            in.skipBytes(in.readableBytes());
            logger.info("Bytes skipped");
            throw exception;
        }

        DocumentFactory df = DocumentFactory.getInstance();
        Document document = df.createDocument();
        Element parent = null;

        while (!streamFeeder.needMoreInput()) {
            int type = streamReader.next();
            switch (type) {
                case XMLStreamConstants.START_DOCUMENT:
//                    logger.info("START DOCUMENT");
                    break;
                case XMLStreamConstants.END_DOCUMENT:
//                    logger.info("End of XML document");
                    logger.info(document.getRootElement().asXML());
                    out.add(document);
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
                        newElement.addNamespace(streamReader.getNamespacePrefix(x), streamReader.getNamespaceURI(x));
                    }
                    // add all attributes to Element
                    for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                        newElement.addAttribute(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i));
                    }
                    if (parent != null) {
                        parent.add(newElement);
                    }
                    else {
                        document.add(newElement);
                    }
                    parent = newElement;

                    break;
                case XMLStreamConstants.END_ELEMENT:
//                    logger.info("END ELEMENT");
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.CHARACTERS:
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.COMMENT:
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.SPACE:
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.DTD:
                    // TODO: Implement if needed.
                    break;
                case XMLStreamConstants.CDATA:
                    // TODO: Implement if needed.
                    break;
            }
        }
        out.add(document);
    }
}
