package com.fasterxml.jackson.dataformat.xml.stream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XmlParser442Test extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    // For [dataformat-xml#442]
    @Test
    public void testMixedContentBeforeElement442() throws Exception
    {
        final String XML =
            "<root>\n" // START_OBJECT
            +"  <branch>\n" // *Missing* START_OBJECT
            +"     text\n"
            +"    <leaf>stuff</leaf>\n"
            +"  </branch>\n" // END_OBJECT
            +"</root>\n" // END_OBJECT
            ;

        // Should get equivalent of:
        //
        // { "branch" : {
        //      "" : "  text  ",
        //      "leaf" : "stuff"
        //    }
        // }

        try (FromXmlParser xp = (FromXmlParser) MAPPER.createParser(XML)) {
            assertToken(JsonToken.START_OBJECT, xp.nextToken());
            assertToken(JsonToken.FIELD_NAME, xp.nextToken());
            assertEquals("branch", xp.currentName());

            // Here's what we are missing:
            assertToken(JsonToken.START_OBJECT, xp.nextToken());
            assertToken(JsonToken.FIELD_NAME, xp.nextToken());
            assertEquals("", xp.currentName());

            assertToken(JsonToken.VALUE_STRING, xp.nextToken());
            assertEquals("text", xp.getText().trim());

            assertToken(JsonToken.FIELD_NAME, xp.nextToken());
            assertEquals("leaf", xp.currentName());
            assertToken(JsonToken.VALUE_STRING, xp.nextToken());
            assertEquals("stuff", xp.getText().trim());
            
            assertToken(JsonToken.END_OBJECT, xp.nextToken());
            assertToken(JsonToken.END_OBJECT, xp.nextToken());

            assertNull(xp.nextToken());
        }
    }
}
