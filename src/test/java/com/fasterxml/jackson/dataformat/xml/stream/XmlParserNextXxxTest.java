package com.fasterxml.jackson.dataformat.xml.stream;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

public class XmlParserNextXxxTest extends XmlTestBase
{
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // [dataformat-xml#204]
    public void testXmlAttributesWithNextTextValue() throws Exception
    {
        final String XML = "<data max=\"7\" offset=\"9\"/>";

        JsonParser xp = (FromXmlParser) xmlMapper(false).createParser(XML);

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
}
