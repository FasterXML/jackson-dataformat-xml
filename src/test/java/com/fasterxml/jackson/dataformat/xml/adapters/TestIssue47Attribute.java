package com.fasterxml.jackson.dataformat.xml.adapters;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestIssue47Attribute extends XmlTestBase
{
    public static class Response
    {
        @JacksonXmlProperty(localName = "wrapper")
        public List<Item> items;
    }

    public static class Item
    {
        public String id;
        public String a;
        public String b;
    }

    public void testEmptyStringFromElemAndAttr() throws Exception
    {
        final XmlMapper MAPPER = new XmlMapper();
        String xml = "<response><wrapper><item id=\"1\"><a>x</a><b>y</b></item><item id=\"2\"><a>y</a><b>x</b></item></wrapper></response>";
        Response res = MAPPER.readValue(xml, Response.class);

//System.out.println(MAPPER.writeValueAsString(res));
        assertNotNull(res.items);
        assertNotNull(res.items.get(0));
        assertNotNull(res.items.get(0).id);
    }
}
