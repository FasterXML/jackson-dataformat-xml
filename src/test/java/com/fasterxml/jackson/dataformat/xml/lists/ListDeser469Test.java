package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// Trying to reproduce [dataformat-xml#469]
public class ListDeser469Test extends XmlTestBase
{
    static class OuterBean {
        @JacksonXmlProperty(localName = "Middle", namespace = "http://jackson.test.model")
        public MiddleBean middle;
    }

    @JsonPropertyOrder({"inner1", "inner2"})
    static class MiddleBean
    {
        @JacksonXmlProperty(localName = "Inner1", namespace = "http://jackson.test.model")
        public InnerBean1 inner1;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Inner2", namespace = "http://jackson.test.model")
        public List<InnerBean2> inner2;
    }

    static class InnerBean1
    {
        @JacksonXmlProperty(localName = "Str", isAttribute = true)
        public String str;

        @JacksonXmlProperty(localName = "InnerBean1Item", namespace = "http://jackson.test.model")
        public List<InnerBean1Item> item;
    }

    static class InnerBean1Item
    {
        @JacksonXmlProperty(localName = "Id", isAttribute = true)
        public String id;
    }

    static class InnerBean2
    {
        @JacksonXmlProperty(localName = "Str2", isAttribute = true)
        public String str2;

        protected InnerBean2() { }
        public InnerBean2(String s) { str2 = s; }
    }

    // also wrt [dataformat-xml#469]
    static class OuterNoWrappers {
        public List<InnerNoWrappers> inner;
    }

    static class InnerNoWrappers {
        @JacksonXmlProperty(isAttribute = true)
        public String str;

        protected InnerNoWrappers() { }
        public InnerNoWrappers(String s) { str = s; }
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
        String xmlInput = "<OuterBean xmlns='http://jackson.test.model'>\n" +
            "  <Middle>\n" +
            "    <Inner1/>\n" +
            "    <Inner2 Str2='aaaa'/>\n" +
            "  </Middle>\n" +
            "</OuterBean>\n";

        OuterBean outer = mapper.readValue(xmlInput, OuterBean.class);

        MiddleBean mid = outer.middle;
        assertNotNull(mid);

        assertNotNull(mid.inner2);
        assertEquals(1, mid.inner2.size());
        assertEquals("aaaa", mid.inner2.get(0).str2);
    }

    // But alternatively can try setting default to "no wrappers":
    public void testIssue469WithNoWrapper() throws Exception
    {
        final XmlMapper mapper = XmlMapper.builder()
                .annotationIntrospector(new JacksonXmlAnnotationIntrospector(false))
                .build();

        // First: check round-trip
        {
            OuterNoWrappers source = new OuterNoWrappers();
            source.inner = new ArrayList<>();
            source.inner.add(new InnerNoWrappers("value"));

            String xml = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(source);
//System.err.println("XML:\n"+xml);
            OuterNoWrappers result = mapper.readValue(xml, OuterNoWrappers.class);
            assertNotNull(result.inner);
            assertEquals(1, result.inner.size());
            assertEquals("value",result.inner.get(0).str);
        }
    }
}
