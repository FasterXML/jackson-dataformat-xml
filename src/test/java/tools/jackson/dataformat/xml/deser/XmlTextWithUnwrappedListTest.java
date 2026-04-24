package tools.jackson.dataformat.xml.deser;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// Regression: a bean with @JacksonXmlText AND an unwrapped collection would
// lose virtual-wrapping support because _modifyBeanDeserializer returned
// XmlTextDeserializer without also applying WrapperHandlingDeserializer.
public class XmlTextWithUnwrappedListTest extends XmlTestUtil
{
    static class StringListContainer {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> items;

        @JacksonXmlText
        public String text;
    }

    static class Item {
        public String name;
        public int value;
    }

    static class PojoListContainer {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Item> items;

        @JacksonXmlText
        public String text;
    }

    static class ArrayContainer {
        @JacksonXmlElementWrapper(useWrapping = false)
        public String[] items;

        @JacksonXmlText
        public String text;
    }

    // No per-property wrapping annotation; relies on mapper-level
    // defaultUseWrapper(false) to unwrap the list.
    static class DefaultUnwrappedContainer {
        public List<String> items;

        @JacksonXmlText
        public String text;
    }

    private final ObjectMapper MAPPER = newMapper();

    @Test
    public void testTextWithUnwrappedStringList() throws Exception {
        String xml = "<StringListContainer>some text<items>a</items><items>b</items></StringListContainer>";
        StringListContainer result = MAPPER.readValue(xml, StringListContainer.class);
        assertNotNull(result);
        assertEquals("some text", result.text);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertEquals("a", result.items.get(0));
        assertEquals("b", result.items.get(1));
    }

    @Test
    public void testTextWithUnwrappedPojoList() throws Exception {
        String xml = "<PojoListContainer>preface"
                + "<items><name>first</name><value>1</value></items>"
                + "<items><name>second</name><value>2</value></items>"
                + "</PojoListContainer>";
        PojoListContainer result = MAPPER.readValue(xml, PojoListContainer.class);
        assertNotNull(result);
        assertEquals("preface", result.text);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertEquals("first", result.items.get(0).name);
        assertEquals(1, result.items.get(0).value);
        assertEquals("second", result.items.get(1).name);
        assertEquals(2, result.items.get(1).value);
    }

    @Test
    public void testTextWithUnwrappedArray() throws Exception {
        String xml = "<ArrayContainer>header<items>x</items><items>y</items><items>z</items></ArrayContainer>";
        ArrayContainer result = MAPPER.readValue(xml, ArrayContainer.class);
        assertNotNull(result);
        assertEquals("header", result.text);
        assertNotNull(result.items);
        assertEquals(3, result.items.length);
        assertEquals("x", result.items[0]);
        assertEquals("y", result.items[1]);
        assertEquals("z", result.items[2]);
    }

    // When the XML contains no list items at all, the parser emits a bare
    // VALUE_STRING for the element; XmlTextDeserializer must still handle
    // that path (wrapping becomes irrelevant).
    @Test
    public void testTextOnlyWithListPropertyDeclared() throws Exception {
        String xml = "<StringListContainer>just text</StringListContainer>";
        StringListContainer result = MAPPER.readValue(xml, StringListContainer.class);
        assertNotNull(result);
        assertEquals("just text", result.text);
        assertNull(result.items);
    }

    // Same composition should also work when unwrapping is driven by the
    // mapper-level defaultUseWrapper(false), not a per-property annotation.
    @Test
    public void testTextWithDefaultUseWrapperFalse() throws Exception {
        ObjectMapper mapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .build();
        String xml = "<DefaultUnwrappedContainer>head<items>a</items><items>b</items></DefaultUnwrappedContainer>";
        DefaultUnwrappedContainer result = mapper.readValue(xml, DefaultUnwrappedContainer.class);
        assertNotNull(result);
        assertEquals("head", result.text);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertEquals("a", result.items.get(0));
        assertEquals("b", result.items.get(1));
    }
}
