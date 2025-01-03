package tools.jackson.dataformat.xml.stream;

import tools.jackson.core.JsonToken;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

public class XmlParser442Test extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    // For [dataformat-xml#442]
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
            assertToken(JsonToken.PROPERTY_NAME, xp.nextToken());
            assertEquals("branch", xp.currentName());

            // Here's what we are missing:
            assertToken(JsonToken.START_OBJECT, xp.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, xp.nextToken());
            assertEquals("", xp.currentName());

            assertToken(JsonToken.VALUE_STRING, xp.nextToken());
            assertEquals("text", xp.getString().trim());

            assertToken(JsonToken.PROPERTY_NAME, xp.nextToken());
            assertEquals("leaf", xp.currentName());
            assertToken(JsonToken.VALUE_STRING, xp.nextToken());
            assertEquals("stuff", xp.getString().trim());
            
            assertToken(JsonToken.END_OBJECT, xp.nextToken());
            assertToken(JsonToken.END_OBJECT, xp.nextToken());

            assertNull(xp.nextToken());
        }
    }
}
