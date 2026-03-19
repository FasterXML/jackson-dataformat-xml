package tools.jackson.dataformat.xml.deser;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// [dataformat-xml#608] Fails to instantiate class when only text node is present
// and the target type has @JacksonXmlText plus other element properties.
public class XmlTextOnlyDeser608Test extends XmlTestUtil
{
    static class Root608 {
        public Nested608 nested;
        public Plain608 plain;
    }

    // Has @JacksonXmlText AND other element properties
    static class Nested608 {
        @JacksonXmlProperty(isAttribute = false)
        public String other;

        @JacksonXmlProperty(isAttribute = false)
        public String reallyNotHere;

        @JacksonXmlText
        public String text;
    }

    // Has only @JacksonXmlText, no other properties
    static class Plain608 {
        @JacksonXmlText
        public String text;
    }

    // Has @JacksonXmlText AND an unwrapped list (needs both text + wrapper handling)
    static class WithList608 {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> item;

        @JacksonXmlText
        public String text;
    }

    private final ObjectMapper MAPPER = newMapper();

    // Works: nested has both a child element and text content
    @Test
    public void testNestedWithChildAndText608() throws Exception {
        String xml = "<Root608>"
                + "<nested><other>text</other>The text node.</nested>"
                + "</Root608>";
        Root608 result = MAPPER.readValue(xml, Root608.class);
        assertNotNull(result.nested);
        assertEquals("text", result.nested.other);
        assertEquals("The text node.", result.nested.text);
    }

    // Works: plain type only has @JacksonXmlText
    @Test
    public void testPlainTextOnly608() throws Exception {
        String xml = "<Root608><plain>The text node.</plain></Root608>";
        Root608 result = MAPPER.readValue(xml, Root608.class);
        assertNotNull(result.plain);
        assertEquals("The text node.", result.plain.text);
    }

    // [dataformat-xml#608]: nested type has @JacksonXmlText plus other element
    // properties, but XML only contains text (no child elements).
    @Test
    public void testNestedWithOnlyText608() throws Exception {
        String xml = "<Root608><nested>The text node.</nested></Root608>";
        Root608 result = MAPPER.readValue(xml, Root608.class);
        assertNotNull(result.nested);
        assertNull(result.nested.other);
        assertNull(result.nested.reallyNotHere);
        assertEquals("The text node.", result.nested.text);
    }

    // [dataformat-xml#608]: text-only with unwrapped list — verifies that
    // wrapper handling still works alongside VALUE_STRING text handling
    @Test
    public void testTextWithUnwrappedListTextOnly608() throws Exception {
        String xml = "<WithList608>Just text.</WithList608>";
        WithList608 result = MAPPER.readValue(xml, WithList608.class);
        assertNotNull(result);
        assertEquals("Just text.", result.text);
        assertNull(result.item);
    }

}
