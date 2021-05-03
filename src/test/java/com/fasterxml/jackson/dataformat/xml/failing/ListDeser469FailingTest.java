package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// Actual reproduction of [dataformat-xml#469]: quite specific set up
// needed, alas
public class ListDeser469FailingTest extends XmlTestBase
{
    @JsonPropertyOrder({"inner1", "inner2"})
    @JsonRootName("outer")
    static class Outer469
    {
        public InnerBean1 inner1;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<InnerBean2> inner2;
    }

    static class InnerBean1
    {
        public String str;

        // Fail if (and only if!) wrapping disabled, by default or via
        // annotations
        // (that is; if this is commented out, passes)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<InnerBean1Item> item;
    }

    static class InnerBean1Item
    {
        @JacksonXmlProperty(isAttribute = true)
        public String id;
    }

    static class InnerBean2
    {
        @JacksonXmlProperty(isAttribute = true)
        public String str2;

        protected InnerBean2() { }
        public InnerBean2(String s) { str2 = s; }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testIssue469WithDefaults() throws Exception
    {
        // Here we just use default settings (which defaults to using wrappers)
        final XmlMapper mapper = newMapper();

        // First: create POJO value to test round-trip:
        if (true) {
            Outer469 source = new Outer469();
            List<InnerBean2> items = new ArrayList<>();
            items.add(new InnerBean2("foo"));
            source.inner2 = items;

            String xml = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(source);
//System.err.println("XML: \n"+xml);
            Outer469 result = mapper.readValue(xml, Outer469.class);

            assertNotNull(result);
            assertNotNull(result.inner2);
            assertEquals(1, result.inner2.size());
            assertEquals("foo", result.inner2.get(0).str2);
        }

        // And then verify from XML String
        String xmlInput =
            "<outer>\n" +
            "  <inner1/>\n" +
            "  <inner2 str2='aaaa'/>\n" +
//            "  <inner2><str2>aaaa</str2></inner2>\n" +
            "</outer>\n";

        Outer469 result = mapper.readValue(xmlInput, Outer469.class);
        assertNotNull(result);

        assertNotNull(result.inner2);
        assertEquals(1, result.inner2.size());
        assertEquals("aaaa", result.inner2.get(0).str2);
    }
}
