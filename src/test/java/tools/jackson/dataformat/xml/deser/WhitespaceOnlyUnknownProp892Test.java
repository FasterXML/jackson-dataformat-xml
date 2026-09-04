package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#892]: whitespace-only element body on an attribute-only type
// must not be treated as unknown property ""
public class WhitespaceOnlyUnknownProp892Test extends XmlTestUtil
{
    record Response(
            @JsonProperty("Item") Item item
    ) {
    }

    record Item(String name) {
    }

    static class ItemPojo {
        public String name;
    }

    private final XmlMapper MAPPER = mapperBuilder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final String ISSUE_XML = """
            <Response>
            \t<Item name="example">
            \t</Item>
            </Response>
            """;

    @Test
    public void testRecordWhitespaceOnlyBody892() throws Exception
    {
        Response response = MAPPER.readValue(ISSUE_XML, Response.class);
        assertEquals(new Item("example"), response.item());
    }

    @Test
    public void testPojoWhitespaceOnlyBody892() throws Exception
    {
        ItemPojo item = MAPPER.readValue("<Item name=\"example\">\n\t</Item>", ItemPojo.class);
        assertEquals("example", item.name);
    }

    @Test
    public void testNonWhitespaceTextStillUnknown892() throws Exception
    {
        try {
            MAPPER.readValue("<Item name=\"example\">secret</Item>", ItemPojo.class);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "Unrecognized property \"\"");
        }
    }
}
