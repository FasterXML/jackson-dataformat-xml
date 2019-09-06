package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TypeAttributeOrder242Test extends XmlTestBase
{
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = B.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = B.class, name = "B")
    })
    static abstract class A {
        @JacksonXmlProperty(isAttribute = true)
        public Integer id;
    }

    static class Attr {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Param> param;
    }

    static class Param {
        public String name;
    }

    static class B extends A {
        public Attr attr;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    public void testAttributeOrder() throws Exception
    {
        String content1 = "<A type='B' id='1'><attr><param name='1'/><param name='2'/></attr></A>";
        B b1 = (B) MAPPER.readValue(content1, A.class);
        assertEquals(2, b1.attr.param.size());
        String content2 = "<A id='1' type='B'><attr><param name='1'/><param name='2'/></attr></A>";
        B b2 = (B) MAPPER.readValue(content2, A.class);
        assertEquals(2, b2.attr.param.size());
    }
}
