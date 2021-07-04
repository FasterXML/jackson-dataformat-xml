package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

// for [dataformat-xml#441]
public class JsonNodeBasicSer441Test extends XmlTestBase
{
    static class Stuff441 {
        @JsonUnwrapped
        public ObjectNode node;

        public Stuff441(ObjectNode n) { node = n; }
    }

    private final ObjectMapper XML_MAPPER = newMapper();

    private final ObjectWriter XML_WRITER_WRAP = XML_MAPPER.writer()
            .without(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE);
    private final ObjectWriter XML_WRITER_UNWRAP = XML_MAPPER.writer()
            .with(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE);

    public void testSimpleNode() throws Exception
    {
        ObjectNode xml = XML_MAPPER.createObjectNode();
        ObjectNode root = xml.putObject("root");
        root.put("id", 13);
        root.put("enabled", true);

        final String INNER = "<root><id>13</id><enabled>true</enabled></root>";

        assertEquals("<ObjectNode>"+INNER+"</ObjectNode>",
                XML_WRITER_WRAP.writeValueAsString(xml));
        assertEquals(INNER,
                XML_WRITER_UNWRAP.writeValueAsString(xml));
    }

    public void testArrayInObjectNode() throws Exception
    {
        ObjectNode xml = XML_MAPPER.createObjectNode();
        ObjectNode root = xml.putObject("root");
        ArrayNode arr = root.putArray("array");
        arr.add("first");
        ObjectNode second = arr.addObject();
        second.put("value", 137);

        final String INNER = "<root><array>first</array><array><value>137</value></array></root>";

//System.err.println("XML/array: "+XML_MAPPER.writeValueAsString(xml));
        assertEquals("<ObjectNode>"+INNER+"</ObjectNode>",
                XML_WRITER_WRAP.writeValueAsString(xml));
        assertEquals(INNER,
                XML_WRITER_UNWRAP.writeValueAsString(xml));
    }

    // 03-Jul-2021, tatu: Would be great to further support "unwrapping" of
    //    properties further down but... for now not very likely to work
    //    but see [databind#3961] for possible improvements
    public void testNodeAsProperty() throws Exception
    {
        Stuff441 stuff = new Stuff441(XML_MAPPER.createObjectNode());
        stuff.node.put("key", "value");
//System.err.println("XML/object: "+XML_WRITER.writeValueAsString(stuff));

        /*
        assertEquals("<Stuff441><key>value</key></Stuff441>",
                XML_MAPPER.writeValueAsString(stuff));
                */
    }
}
