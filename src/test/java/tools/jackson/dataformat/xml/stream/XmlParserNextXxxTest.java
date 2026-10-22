package tools.jackson.dataformat.xml.stream;

import org.junit.jupiter.api.Test;

import tools.jackson.core.*;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    // [dataformat-xml#899]: nextStringValue() must honor the JsonParser contract
    // at end of input: return null, same as nextToken() does, instead of leaking
    // an unchecked IllegalStateException from the internal XML_END branch.
    @Test
    public void testNextStringValueAtEndOfInput() throws Exception
    {
        final String XML = "<data max=\"7\" offset=\"9\"/>";

        JsonParser xp = xmlMapper(false).createParser(XML);

        assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
        assertToken(JsonToken.PROPERTY_NAME, xp.nextToken()); // max
        assertEquals("7", xp.nextStringValue());
        assertToken(JsonToken.PROPERTY_NAME, xp.nextToken()); // offset
        assertEquals("9", xp.nextStringValue());
        assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>

        // One more call past the end: should quietly report end-of-input
        assertNull(xp.nextStringValue());
        xp.close();
    }
}
