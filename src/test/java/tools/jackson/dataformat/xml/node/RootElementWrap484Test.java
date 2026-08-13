package tools.jackson.dataformat.xml.node;

import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.node.ObjectNode;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// [dataformat-xml#484]: WRAP_ROOT_ELEMENT_NAME exposes root element as outer wrapper
public class RootElementWrap484Test extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    private final ObjectReader WRAP_READER = MAPPER.reader()
            .with(XmlReadFeature.WRAP_ROOT_ELEMENT_NAME);
    private final ObjectReader PLAIN_READER = MAPPER.reader();

    // Object root with single child element → {"root":{"value":"3"}}
    @Test
    public void testObjectRootWrapped() throws Exception
    {
        JsonNode tree = WRAP_READER.readTree("<root><value>3</value></root>");
        assertTrue(tree.isObject(), "expected outer Object, got: " + tree);
        assertEquals(1, tree.size());
        JsonNode inner = tree.get("root");
        assertTrue(inner.isObject(), "expected inner Object, got: " + inner);
        assertEquals("3", inner.get("value").asString());
    }

    // Object root with multiple children + attributes
    @Test
    public void testObjectRootWithAttributesAndChildren() throws Exception
    {
        JsonNode tree = WRAP_READER.readTree(
                "<root id=\"1\"><a>x</a><b>y</b></root>");
        JsonNode inner = tree.get("root");
        assertEquals("1", inner.get("id").asString());
        assertEquals("x", inner.get("a").asString());
        assertEquals("y", inner.get("b").asString());
    }

    // Text-only root: wrap is purely token-level, so body matches what you
    // would get without wrap (text becomes an empty-name property).
    // <root>3</root> unwrapped → {"":"3"}; wrapped → {"root":{"":"3"}}
    @Test
    public void testTextOnlyRootWrapped() throws Exception
    {
        JsonNode tree = WRAP_READER.readTree("<root>3</root>");
        assertTrue(tree.isObject());
        JsonNode inner = tree.get("root");
        assertTrue(inner.isObject(), "inner expected to be Object, got: " + inner);
        assertEquals("3", inner.get("").asString());
    }

    // xsi:nil root → {"root":null}
    @Test
    public void testXsiNilRootWrapped() throws Exception
    {
        JsonNode tree = WRAP_READER.readTree(
                "<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:nil=\"true\"/>");
        assertTrue(tree.isObject());
        assertTrue(tree.get("root").isNull());
    }

    // Empty element with EMPTY_ELEMENT_AS_NULL → {"root":null}
    @Test
    public void testEmptyElementAsNullRootWrapped() throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .with(XmlReadFeature.WRAP_ROOT_ELEMENT_NAME)
                .with(XmlReadFeature.EMPTY_ELEMENT_AS_NULL);
        JsonNode tree = r.readTree("<root/>");
        assertTrue(tree.isObject());
        assertTrue(tree.get("root").isNull());
    }

    // Map binding works equivalently
    @Test
    public void testMapBindingWrapped() throws Exception
    {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> result = WRAP_READER.forType(Map.class)
                .readValue("<root><value>3</value></root>");
        assertEquals(1, result.size());
        assertEquals("3", result.get("root").get("value"));
    }

    // Round-trip: parse-with-wrap → serialize-with-unwrap returns to original XML
    @Test
    public void testRoundTrip() throws Exception
    {
        final String INPUT = "<root><value>3</value></root>";
        JsonNode tree = WRAP_READER.readTree(INPUT);
        // UNWRAP_ROOT_OBJECT_NODE is on by default in 3.x
        ObjectWriter w = MAPPER.writer().with(XmlWriteFeature.UNWRAP_ROOT_OBJECT_NODE);
        String xml = w.writeValueAsString(tree);
        assertEquals(INPUT, xml);
    }

    // Default off: existing behavior unchanged
    @Test
    public void testDefaultOffUnchanged() throws Exception
    {
        JsonNode tree = PLAIN_READER.readTree("<root><value>3</value></root>");
        // Without wrap, the root element name is dropped — body is exposed directly
        assertTrue(tree.isObject());
        assertFalse(tree.has("root"));
        assertEquals("3", tree.get("value").asString());
    }

    // Sanity: explicitly disabling the feature behaves like default
    @Test
    public void testFeatureExplicitlyDisabled() throws Exception
    {
        ObjectReader r = MAPPER.reader().without(XmlReadFeature.WRAP_ROOT_ELEMENT_NAME);
        JsonNode tree = r.readTree("<root><value>3</value></root>");
        assertEquals("3", tree.get("value").asString());
        assertFalse(tree.has("root"));
    }

    // ObjectNode round-trip starting from constructed tree
    @Test
    public void testObjectNodeRoundTrip() throws Exception
    {
        ObjectNode wrapper = MAPPER.createObjectNode();
        ObjectNode inner = wrapper.putObject("root");
        inner.put("a", "1");
        inner.put("b", "2");
        ObjectWriter w = MAPPER.writer().with(XmlWriteFeature.UNWRAP_ROOT_OBJECT_NODE);
        String xml = w.writeValueAsString(wrapper);
        assertEquals("<root><a>1</a><b>2</b></root>", xml);

        JsonNode reparsed = WRAP_READER.readTree(xml);
        assertEquals(wrapper, reparsed);
    }
}
