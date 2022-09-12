package com.fasterxml.jackson.dataformat.xml.stream;

import java.io.*;

import javax.xml.stream.*;

import com.fasterxml.jackson.core.io.ContentReference;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlNameProcessors;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlTokenStream;


// NOTE: test changed a lot between 2.13 and 2.14:
public class XmlTokenStreamTest extends XmlTestBase
{
    private final XmlFactory XML_FACTORY = newMapper().getFactory();

    public void testSimple() throws Exception
    {
        XmlTokenStream tokens = _tokensFor("<root><leaf id='123'>abc</leaf></root>");
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.getCurrentToken());
        assertEquals("root", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("leaf", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokens.next());
        assertEquals("id", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokens.next());
        assertEquals("123", tokens.getText());
        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("abc", tokens.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        // 23-May-2020, tatu: Not known for END_ELEMENT, alas, so:
        assertEquals("", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals("", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_END, tokens.next());
    }

    public void testRootAttributes() throws Exception
    {
        _testRootAttributes(true); // empty tag as null
        _testRootAttributes(false); // empty tag as ""
    }

    public void _testRootAttributes(boolean emptyAsNull) throws Exception
    {
        String XML = "<root id='x' />";
        int f = FromXmlParser.Feature.collectDefaults();
        if (emptyAsNull) {
            f |= FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        } else {
            f &= ~FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        }
        XmlTokenStream tokens = _tokensFor(XML, f);
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.getCurrentToken());
        assertEquals("root", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokens.next());
        assertEquals("id", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokens.next());
        assertEquals("x", tokens.getText());
        if (!emptyAsNull) {
            assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
            assertEquals("", tokens.getText());
        }
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END, tokens.next());
    }

    public void testEmptyTags() throws Exception
    {
        _testEmptyTags(true); // empty tag as null
        _testEmptyTags(false); // empty tag as ""
    }
        
    private void _testEmptyTags(boolean emptyAsNull) throws Exception
    {
        String XML = "<root><leaf /></root>";
        int f = FromXmlParser.Feature.collectDefaults();
        if (emptyAsNull) {
            f |= FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        } else {
            f &= ~FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        }
        XmlTokenStream tokens = _tokensFor(XML, f);
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.getCurrentToken());
        assertEquals("root", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("leaf", tokens.getLocalName());
        if (!emptyAsNull) {
            assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
            assertEquals("", tokens.getText());
        }
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END, tokens.next());
    }

    public void testNested() throws Exception
    {
        XmlTokenStream tokens = _tokensFor( "<root><a><b><c>abc</c></b></a></root>");
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.getCurrentToken());
        assertEquals("root", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("a", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("b", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("c", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("abc", tokens.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END, tokens.next());
    }

    // For [dataformat-xml#402]
    public void testMixedContentBetween() throws Exception
    {
        XmlTokenStream tokens = _tokensFor("<root>first<a>123</a> and second <b>abc</b>\n</root>");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.getCurrentToken());
        assertEquals("root", tokens.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("first", tokens.getText());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("a", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("123", tokens.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());

        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals(" and second ", tokens.getText());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("b", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("abc", tokens.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END, tokens.next());
    }

    // For [dataformat-xml#402]
    public void testMixedContentAfter() throws Exception
    {
        XmlTokenStream tokens = _tokensFor("<root>first<a>123</a>last &amp; final</root>");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.getCurrentToken());
        assertEquals("root", tokens.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("first", tokens.getText());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokens.next());
        assertEquals("a", tokens.getLocalName());
        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("123", tokens.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());

        assertEquals(XmlTokenStream.XML_TEXT, tokens.next());
        assertEquals("last & final", tokens.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
        assertEquals(XmlTokenStream.XML_END, tokens.next());
    }

    private XmlTokenStream _tokensFor(String doc) throws Exception {
        return _tokensFor(doc, FromXmlParser.Feature.collectDefaults());
    }

    private XmlTokenStream _tokensFor(String doc, int flags) throws Exception
    {
        XMLStreamReader sr = XML_FACTORY.getXMLInputFactory().createXMLStreamReader(new StringReader(doc));
        // must point to START_ELEMENT, so:
        sr.nextTag();
        XmlTokenStream stream = new XmlTokenStream(sr, ContentReference.rawReference(doc), flags, XmlNameProcessors.newPassthroughProcessor());
        stream.initialize();
        return stream;
    }
}
