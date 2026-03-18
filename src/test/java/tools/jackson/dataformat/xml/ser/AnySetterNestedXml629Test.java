package tools.jackson.dataformat.xml.ser;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#629] @JsonAnySetter mangles nested XML elements and attributes
public class AnySetterNestedXml629Test extends XmlTestUtil
{
    static class PojoWith629 {
        @JsonProperty("id")
        public String id;

        @JsonAnySetter
        @JsonAnyGetter
        public Map<String, Object> others = new LinkedHashMap<>();
    }

    private final XmlMapper MAPPER = newMapper();

    // Simple unmapped elements round-trip correctly
    @Test
    public void testAnySetterSimpleRoundTrip() throws Exception
    {
        String input = "<PojoWith629><id>123</id><simple>hello</simple></PojoWith629>";
        PojoWith629 pojo = MAPPER.readValue(input, PojoWith629.class);
        assertEquals("123", pojo.id);
        assertEquals("hello", pojo.others.get("simple"));

        String xml = MAPPER.writeValueAsString(pojo);
        assertTrue(xml.contains("<simple>hello</simple>"), "Got: " + xml);
    }

    // Nested elements without attributes round-trip correctly
    @Test
    public void testAnySetterNestedElementsRoundTrip() throws Exception
    {
        String input =
            "<PojoWith629>" +
                "<id>123</id>" +
                "<unmapped>" +
                    "<a>1</a>" +
                    "<b>2</b>" +
                "</unmapped>" +
            "</PojoWith629>";

        PojoWith629 pojo = MAPPER.readValue(input, PojoWith629.class);
        assertEquals("123", pojo.id);

        Object unmapped = pojo.others.get("unmapped");
        assertNotNull(unmapped, "unmapped should be captured by @JsonAnySetter");
        assertTrue(unmapped instanceof Map, "Expected Map but got: " + unmapped.getClass());
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) unmapped;
        assertEquals("1", nested.get("a"));
        assertEquals("2", nested.get("b"));

        String xml = MAPPER.writeValueAsString(pojo);
        assertFalse(xml.contains("<>"), "Output contains empty tags: " + xml);
        assertFalse(xml.contains("</>"), "Output contains empty closing tags: " + xml);
    }

    // [dataformat-xml#629]: Elements with attributes should not produce empty tags
    @Test
    public void testAnySetterAttributeElementRoundTrip() throws Exception
    {
        String input =
            "<PojoWith629>" +
                "<id>123</id>" +
                "<elem uid=\"1\">text</elem>" +
            "</PojoWith629>";

        PojoWith629 pojo = MAPPER.readValue(input, PojoWith629.class);
        assertEquals("123", pojo.id);

        String xml = MAPPER.writeValueAsString(pojo);
        assertFalse(xml.contains("<>"), "Output contains empty tags: " + xml);
        assertFalse(xml.contains("</>"), "Output contains empty closing tags: " + xml);
        // text content should be present (even if attribute-ness is lost)
        assertTrue(xml.contains("text"), "Got: " + xml);
    }

    // [dataformat-xml#629]: The reporter's original case
    @Test
    public void testAnySetterNestedElementsWithAttributesRoundTrip() throws Exception
    {
        String input =
            "<PojoWith629>" +
                "<id>123</id>" +
                "<unmapped-element>" +
                    "<e uid=\"1\">one</e>" +
                    "<e uid=\"2\">TWO</e>" +
                    "<e uid=\"3\">3</e>" +
                "</unmapped-element>" +
            "</PojoWith629>";

        PojoWith629 pojo = MAPPER.readValue(input, PojoWith629.class);
        assertEquals("123", pojo.id);

        String xml = MAPPER.writeValueAsString(pojo);
        assertFalse(xml.contains("<>"), "Output contains empty tags: " + xml);
        assertFalse(xml.contains("</>"), "Output contains empty closing tags: " + xml);
    }

    // [dataformat-xml#629]: Verify text-element key works when it appears first
    //   in the Map (edge case: _nextName could be null at that point)
    @Test
    public void testAnySetterTextKeyFirstInMap() throws Exception
    {
        PojoWith629 pojo = new PojoWith629();
        pojo.id = "1";
        // Construct a Map where the empty key (text element) appears first
        LinkedHashMap<String, Object> inner = new LinkedHashMap<>();
        inner.put("", "textval");
        inner.put("attr", "aval");
        pojo.others.put("elem", inner);

        String xml = MAPPER.writeValueAsString(pojo);
        assertFalse(xml.contains("<>"), "Output contains empty tags: " + xml);
        assertFalse(xml.contains("</>"), "Output contains empty closing tags: " + xml);
        assertTrue(xml.contains("textval"), "Text content missing: " + xml);
        assertTrue(xml.contains("<attr>aval</attr>"), "Attribute-turned-element missing: " + xml);
    }
}
