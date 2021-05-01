package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// Actual reproduction of [dataformat-xml#469]: quite specific set up
// needed, alas
public class ListDeser469FailingTest extends XmlTestBase
{
    static class OuterBean {
        public MiddleBean middle;
    }

    @JsonPropertyOrder({"inner1", "inner2"})
    static class MiddleBean
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
        {
            OuterBean source = new OuterBean();
            source.middle = new MiddleBean();
            List<InnerBean2> items = new ArrayList<>();
            items.add(new InnerBean2("foo"));
            source.middle.inner2 = items;

            String xml = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(source);

            OuterBean result = mapper.readValue(xml, OuterBean.class);

            MiddleBean mid = result.middle;
            assertNotNull(mid);
            assertNotNull(mid.inner2);
            assertEquals(1, mid.inner2.size());
            assertEquals("foo", mid.inner2.get(0).str2);
        }

        // And then verify from XML String
        String xmlInput = "<OuterBean>\n" +
            "  <middle>\n" +
            "    <inner1/>\n" +
            "    <inner2 str2='aaaa'/>\n" +
            "  </middle>\n" +
            "</OuterBean>\n";

        OuterBean outer = mapper.readValue(xmlInput, OuterBean.class);

        MiddleBean mid = outer.middle;
        assertNotNull(mid);

        assertNotNull(mid.inner2);
        assertEquals(1, mid.inner2.size());
        assertEquals("aaaa", mid.inner2.get(0).str2);
    }
}
