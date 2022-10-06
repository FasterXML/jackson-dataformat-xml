package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class UntypedObjectDeserTest extends XmlTestBase
{
    private final ObjectMapper XML_MAPPER = newMapper();

    // for [dataformat-xml#205], handling "untyped" ({@code java.lang.Object}-targeted)
    // deserialization, including handling of element sequences
    public void testRepeatingElements() throws Exception
    {
        final String XML =
                "<person>\n" +
                "   <name>John</name>\n" +
                "   <parent>Jose</parent>\n" +
                "   <parent>Maria</parent>\n" +
                "   <dogs>\n" +
                "      <count>3</count>\n" +
                "      <dog>\n" +
                "         <name>Spike</name>\n" +
                "         <age>12</age>\n" +
                "      </dog>\n" +
                "      <dog>\n" +
                "         <name>Brutus</name>\n" +
                "         <age>9</age>\n" +
                "      </dog>\n" +
                "      <dog>\n" +
                "         <name>Bob</name>\n" +
                "         <age>14</age>\n" +
                "      </dog>\n" +
                "   </dogs>\n" +
                "</person>";
        final JsonNode fromXml = XML_MAPPER.valueToTree(XML_MAPPER.readValue(XML, Object.class));
        final ObjectNode exp = XML_MAPPER.createObjectNode();
        exp.put("name", "John");
        {
            exp.putArray("parent")
                .add("Jose")
                .add("Maria");
            ArrayNode dogs = exp.putObject("dogs")
                .put("count", "3")
                .putArray("dog");
            dogs.addObject()
                .put("name", "Spike")
                .put("age", "12");
            dogs.addObject()
                .put("name", "Brutus")
                .put("age", "9");
            dogs.addObject()
                .put("name", "Bob")
                .put("age", "14");
        }
        if (!fromXml.equals(exp)) {
            ObjectWriter w = new JsonMapper().writerWithDefaultPrettyPrinter();
            fail("Expected:\n"+w.writeValueAsString(exp)+"\ngot:\n"+w.writeValueAsString(fromXml));
        }
    }

    // [dataformat-xml#405]: support mixed content
    public void testMixedContent() throws Exception
    {
        final String XML = "<root>first<a>123</a>second<b>abc</b>last</root>";
        final JsonNode fromXml = XML_MAPPER.valueToTree(XML_MAPPER.readValue(XML, Object.class));
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

    // [dataformat-xml#445]: problem with earlier #205 implementation (from 2.12.0),
    // fixed in 2.12.2
    public void testDuplicateListDeser445() throws Exception
    {
        final String XML =
                "<person>\n" +
                "    <name>a</name>\n" +
                "    <name>b</name>\n" +
                "    <surname>c</surname>\n" +
                "    <surname>d</surname>\n" +
                "</person>";
        @SuppressWarnings("unchecked")
        Map<String, List<String>> person = (Map<String, List<String>>) XML_MAPPER.readValue(XML, Object.class);
        List<String> names = person.get("name");
        List<String> surnames = person.get("surname");
        assertEquals(2, names.size());
        assertEquals(Arrays.asList("a", "b"), names);
        assertEquals(2, surnames.size());
        assertEquals(Arrays.asList("c", "d"), surnames);
    }
}
