package com.fasterxml.jackson.dataformat.xml.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class JsonNodeBasicDeserTest extends XmlTestBase
{
    final private ObjectMapper XML_MAPPER = newMapper();

    public void testSimpleNode() throws Exception
    {
        JsonNode root = XML_MAPPER.readTree("<root attr='123' />");
        assertTrue(root.isObject());
        assertEquals(1, root.size());
        assertEquals("123", root.get("attr").textValue());
    }

    // [dataformat-xml#403]: Allow sequences
    public void testRepeated() throws Exception
    {
        JsonNode root = XML_MAPPER.readTree("<root><value>a</value><value>b</value></root>");
        assertTrue(root.isObject());
        JsonNode arr = root.get("value");
        assertEquals(JsonNodeType.ARRAY, arr.getNodeType());
        assertTrue(arr.isArray());
        assertEquals(2, arr.size());
        assertEquals("a", root.at("/value/0").asText());
        assertEquals("b", root.at("/value/1").asText());
    }

    // [dataformat-xml#405]: support mixed content
    public void testMixedContent() throws Exception
    {
        JsonNode fromXml = XML_MAPPER.readTree("<root>first<a>123</a>second<b>abc</b>last</root>");
        final ObjectNode exp = XML_MAPPER.createObjectNode();
        exp.putArray("")
            .add("first")
            .add("second")
            .add("last");
        exp.put("a", "123");
        exp.put("b", "abc");
        
        if (!fromXml.equals(exp)) {
            ObjectWriter w = new JsonMapper().writerWithDefaultPrettyPrinter();
            fail("Expected:\n"+w.writeValueAsString(exp)+"\ngot:\n"+w.writeValueAsString(fromXml));
        }
    }
}
