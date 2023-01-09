package com.fasterxml.jackson.dataformat.xml.util;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.io.Stax2ByteArraySource;
import org.codehaus.stax2.io.Stax2CharArraySource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;

public class Stax2Util {
    public static XMLStreamReader createXMLStreamReader(final XMLInputFactory xmlInputFactory, final byte[] data,
                                                        final int offset, final int len) throws XMLStreamException {
        if (xmlInputFactory instanceof XMLInputFactory2) {
            return xmlInputFactory.createXMLStreamReader(new Stax2ByteArraySource(data, offset, len));
        } else {
            return xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(data, offset, len));
        }
    }

    public static XMLStreamReader createXMLStreamReader(final XMLInputFactory xmlInputFactory, final char[] data,
                                                        final int offset, final int len) throws XMLStreamException {
        if (xmlInputFactory instanceof XMLInputFactory2) {
            return xmlInputFactory.createXMLStreamReader(new Stax2CharArraySource(data, offset, len));
        } else {
            return xmlInputFactory.createXMLStreamReader(new CharArrayReader(data, offset, len));
        }
    }
}
