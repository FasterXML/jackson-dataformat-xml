package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// for [dataformat-xml#205], handling "untyped" ({@code java.lang.Object}-targeted)
// deserialization, including handling of element sequences
public class UntypedObjectDeser205Test extends XmlTestBase
{
    private final ObjectMapper XML_MAPPER = newMapper();

    private final ObjectMapper JSON_MAPPER = new JsonMapper();

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
        final JsonNode fromXml = JSON_MAPPER.valueToTree(XML_MAPPER.readValue(XML, Object.class));
        final ObjectNode exp = JSON_MAPPER.createObjectNode();
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
            ObjectWriter w = JSON_MAPPER.writerWithDefaultPrettyPrinter();
            fail("Expected:\n"+w.writeValueAsString(exp)+"\ngot:\n"+w.writeValueAsString(fromXml));
        }
    }
}
