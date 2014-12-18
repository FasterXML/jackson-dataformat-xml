package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class NodeTest extends XmlTestBase
{
    public void testMixed() throws Exception
    {
        final XmlMapper xmlMapper = new XmlMapper();
        final ObjectMapper jsonMapper = new ObjectMapper();

        JsonNode root = xmlMapper.readTree("<root>first<child>4</child>second</root>");
        String json = jsonMapper.writeValueAsString(root);

        System.out.println("-> "+json);
    }
}
