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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
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
        } catch (Exception exception) {
            in.skipBytes(in.readableBytes());
            throw exception;
        }

        DocumentFactory df = DocumentFactory.getInstance();
        Document document = df.createDocument();
        Element parent = null;

        while (!streamFeeder.needMoreInput()) {
            int type = streamReader.next();
            switch (type) {
                case XMLStreamConstants.START_DOCUMENT:
                    logger.debug("START DOCUMENT");
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    logger.debug("End of XML document");
                    out.add(document);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    logger.debug("Start element");
                    for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                        logger.info("attr1: " + streamReader.getAttributeName(i));
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    break;
                case XMLStreamConstants.COMMENT:
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    break;
                case XMLStreamConstants.DTD:

                    break;
                case XMLStreamConstants.CDATA:

                    break;
            }
        }

    }
}
