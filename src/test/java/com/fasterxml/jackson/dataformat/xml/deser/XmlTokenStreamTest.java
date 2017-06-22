package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.*;

import javax.xml.stream.*;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlTokenStream;

public class XmlTokenStreamTest extends XmlTestBase
{
    private final XMLInputFactory _staxInputFactory = XMLInputFactory.newInstance();

    public void testSimple() throws Exception
    {
        String XML = "<root><leaf id='123'>abc</leaf></root>";
        XMLStreamReader sr = _staxInputFactory.createXMLStreamReader(new StringReader(XML));
        // must point to START_ELEMENT, so:
        sr.nextTag();
        XmlTokenStream tokens = new XmlTokenStream(sr, XML,
                FromXmlParser.Feature.collectDefaults());
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
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokens.next());
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
        XMLStreamReader sr = _staxInputFactory.createXMLStreamReader(new StringReader(XML));
        // must point to START_ELEMENT, so:
        sr.nextTag();
        int f = FromXmlParser.Feature.collectDefaults();
        if (emptyAsNull) {
            f |= FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        } else {
            f &= ~FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        }
        XmlTokenStream tokens = new XmlTokenStream(sr, XML, f);
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
        XMLStreamReader sr = _staxInputFactory.createXMLStreamReader(new StringReader(XML));
        // must point to START_ELEMENT, so:
        sr.nextTag();
        int f = FromXmlParser.Feature.collectDefaults();
        if (emptyAsNull) {
            f |= FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        } else {
            f &= ~FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.getMask();
        }
        XmlTokenStream tokens = new XmlTokenStream(sr, XML, f);
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
        String XML = "<root><a><b><c>abc</c></b></a></root>";
        XMLStreamReader sr = _staxInputFactory.createXMLStreamReader(new StringReader(XML));
        sr.nextTag();
        XmlTokenStream tokens = new XmlTokenStream(sr, XML,
                FromXmlParser.Feature.collectDefaults());
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
    
}
