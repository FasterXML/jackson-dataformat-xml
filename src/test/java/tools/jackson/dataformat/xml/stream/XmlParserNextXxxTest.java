package tools.jackson.dataformat.xml.stream;

import org.junit.jupiter.api.Test;

import tools.jackson.core.*;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlParserNextXxxTest extends XmlTestUtil
{
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

        JsonParser xp = (FromXmlParser) xmlMapper(false).createParser(XML);

        // First: verify handling without forcing array handling:
        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
        assertToken(JsonToken.PROPERTY_NAME, xp.nextToken()); // <max>
        assertEquals("max", xp.currentName());

        assertEquals("7", xp.nextStringValue());

        assertToken(JsonToken.PROPERTY_NAME, xp.nextToken()); // <offset>
        assertEquals("offset", xp.currentName());

        assertEquals("offset", xp.getString());

        assertEquals("9", xp.nextStringValue());

        assertEquals("9", xp.getString());

        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
        xp.close();
    }
}
