package com.fasterxml.jackson.dataformat.xml.stream;

import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XmlParserNextXxxTest extends XmlTestUtil
{
    protected JsonFactory _jsonFactory;
    protected XmlFactory _xmlFactory;

    // let's actually reuse XmlMapper to make things bit faster
    @BeforeEach
    public void setUp() throws Exception {
        _xmlFactory = new XmlFactory();
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // [dataformat-xml#204]
    @Test
    public void testXmlAttributesWithNextTextValue() throws Exception
    {
        final String XML = "<data max=\"7\" offset=\"9\"/>";

        FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));

        // First: verify handling without forcing array handling:
        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <max>
        assertEquals("max", xp.currentName());

        assertEquals("7", xp.nextTextValue());

        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <offset>
        assertEquals("offset", xp.currentName());

        assertEquals("offset", xp.getText());

        assertEquals("9", xp.nextTextValue());

        assertEquals("9", xp.getText());

        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
        xp.close();
    }

    // [dataformat-xml#899]: nextTextValue() must honor the JsonParser contract
    // at end of input: return null, same as nextToken() does, instead of leaking
    // an unchecked IllegalStateException from the internal XML_END branch.
    @Test
    public void testNextTextValueAtEndOfInput() throws Exception
    {
        final String XML = "<data max=\"7\" offset=\"9\"/>";

        FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));

        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // max
        assertEquals("7", xp.nextTextValue());
        assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // offset
        assertEquals("9", xp.nextTextValue());
        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>

        // One more call past the end: should quietly report end-of-input
        assertNull(xp.nextTextValue());
        xp.close();
    }
}
