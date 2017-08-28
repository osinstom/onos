package org.onosproject.xmpp.ctl;


import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.evt.EventAllocatorImpl;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

/**
 * Decodes an XMPP message for netty pipeline
 */
public class XmppDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AsyncXMLInputFactory XML_INPUT_FACTORY = new InputFactoryImpl();
//    private static final XmlDocumentEnd XML_DOCUMENT_END = XmlDocumentEnd.INSTANCE;

    private final AsyncXMLStreamReader streamReader = XML_INPUT_FACTORY.createAsyncForByteArray();
    private final AsyncByteArrayFeeder streamFeeder = (AsyncByteArrayFeeder) streamReader.getInputFeeder();
    private final EventAllocatorImpl allocator = EventAllocatorImpl.getDefaultInstance();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        byte[] buffer = new byte[in.readableBytes()];
        in.readBytes(buffer);
        try {
            streamFeeder.feedInput(buffer, 0, buffer.length);
        } catch (XMLStreamException exception) {
            in.skipBytes(in.readableBytes());
            throw exception;
        }

        while (streamReader.hasNext() && streamReader.next() != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
            out.add(allocator.allocate(streamReader));
        }

    }
}
