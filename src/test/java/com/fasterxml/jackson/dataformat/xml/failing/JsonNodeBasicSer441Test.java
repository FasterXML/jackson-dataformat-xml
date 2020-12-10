package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// for [dataformat-xml#441]
public class JsonNodeBasicSer441Test extends XmlTestBase
{
    final private ObjectMapper XML_MAPPER = newMapper();

    public void testSimpleNode() throws Exception
    {
        ObjectNode xml = XML_MAPPER.createObjectNode();
        ObjectNode root = xml.putObject("root");
        root.put("id", 13);
        root.put("enabled", true);
//System.err.println("XML/object: "+XML_MAPPER.writeValueAsString(xml));
        assertEquals("<root><id>13</id><enabled>true</enabled></root>",
                XML_MAPPER.writeValueAsString(xml));
    }

    public void testArrayNode() throws Exception
    {
        ObjectNode xml = XML_MAPPER.createObjectNode();
        ObjectNode root = xml.putObject("root");
        ArrayNode arr = root.putArray("array");
        arr.add("first");
        ObjectNode second = arr.addObject();
        second.put("value", 137);
        
//System.err.println("XML/array: "+XML_MAPPER.writeValueAsString(xml));
        assertEquals("<root><array>first</array><array><value>137</value></array></root>",
                XML_MAPPER.writeValueAsString(xml));
    }
}
