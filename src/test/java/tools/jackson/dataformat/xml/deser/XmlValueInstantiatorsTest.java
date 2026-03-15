package tools.jackson.dataformat.xml.deser;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#615]: Additional coverage for XmlValueInstantiators
public class XmlValueInstantiatorsTest extends XmlTestUtil
{
    static class FieldTextWithCreator {
        @JacksonXmlText
        String value;

        @JacksonXmlProperty(isAttribute = true, localName = "attr")
        String attr;

        @JsonCreator
        FieldTextWithCreator(@JsonProperty("value") String value,
                             @JsonProperty("attr") String attr) {
            this.value = value;
            this.attr = attr;
        }
    }

    record RecordTextAndAttribute(@JacksonXmlText String value,
                  @JacksonXmlProperty(isAttribute = true, localName = "Ccy") String currency) {}

    @JsonRootName("Mixed")
    static class MixedTextAndWrapper {
        final String text;
        final List<String> items;

        @JsonCreator
        MixedTextAndWrapper(
                @JacksonXmlText String text,
                @JacksonXmlElementWrapper(localName = "items")
                @JacksonXmlProperty(localName = "item")
                List<String> items) {
            this.text = text;
            this.items = items;
        }
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testFieldTextWithCreator() throws Exception
    {
        final String XML = "<FieldTextWithCreator attr='hello'>world</FieldTextWithCreator>";
        FieldTextWithCreator result = MAPPER.readValue(XML, FieldTextWithCreator.class);
        assertEquals("world", result.value);
        assertEquals("hello", result.attr);
    }

    @Test
    public void testRecordRoundTrip() throws Exception
    {
        RecordTextAndAttribute original = new RecordTextAndAttribute("100", "USD");
        String xml = MAPPER.writeValueAsString(original);
        RecordTextAndAttribute result = MAPPER.readValue(xml, RecordTextAndAttribute.class);
        assertEquals(original.value(), result.value());
        assertEquals(original.currency(), result.currency());
    }

    @Test
    public void testMixedTextAndWrapper() throws Exception
    {
        final String XML =
                "<Mixed>" +
                    "<items>" +
                        "<item>a</item>" +
                        "<item>b</item>" +
                    "</items>" +
                "</Mixed>";
        MixedTextAndWrapper result = MAPPER.readValue(XML, MixedTextAndWrapper.class);
        assertNotNull(result);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertEquals("a", result.items.get(0));
        assertEquals("b", result.items.get(1));
    }
}
