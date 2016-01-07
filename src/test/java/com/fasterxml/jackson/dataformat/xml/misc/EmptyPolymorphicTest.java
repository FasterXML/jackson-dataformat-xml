package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// test(s) for [dataformat-xml#111]
public class EmptyPolymorphicTest extends XmlTestBase
{
    static class Data {
        public String name;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
        @JsonSubTypes({ @JsonSubTypes.Type(EmptyProxy.class) })
        public Proxy proxy;

        public Data() { }
        public Data(String n) {
            name = n;
            proxy = new EmptyProxy();
        }
    }

    static interface Proxy { }

    @JsonTypeName("empty")
    static class EmptyProxy implements Proxy { }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    
    protected XmlMapper MAPPER = new XmlMapper();
    
    public void testEmpty() throws Exception
    {
        String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new Data("Foobar"));
//System.out.println("XML:\n"+xml);
        final Data data = MAPPER.readValue(xml, Data.class);
//                "<data><name>Foobar</name><proxy><empty></empty></proxy></data>"
        assertNotNull(data);
    }
}
