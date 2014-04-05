package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class ListWithAttributes extends XmlTestBase
{
    // [Issue#99]: allow skipping unknown properties
    static class Root {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "value")
        public List<Value> values;
    }

    public static class Value {
        @JacksonXmlProperty(isAttribute = true)
        public int id;
    }

    // [Issue#108]: unwrapped lists, more than one entry, id attributes
    
    static class Foo {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Bar> firstBar = new ArrayList<Bar>();
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Bar> secondBar = new ArrayList<Bar>();
    }

    static class Bar {
        public String value;

        @JacksonXmlProperty(isAttribute = true)
        public int id;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    // [Issue#99]: allow skipping unknown properties
    public void testListWithAttributes() throws Exception
    {
        String source = "<Root>"
                + "     <value id=\"1\"/>"
                + "     <fail/>"
                + "</Root>";
        ObjectMapper mapper = new XmlMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Root root = mapper.readValue(source, Root.class);
        mapper.writeValue(System.out, root);
        assertNotNull(root.values);
        assertEquals(1, root.values.size());
    }
    
    // [Issue#108]: unwrapped lists, more than one entry, id attributes
    public void testIdsFromAttributes() throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        Foo foo = new Foo();
        Bar bar1 = new Bar();
        bar1.id = 1;
        bar1.value = "FIRST";
        foo.firstBar.add(bar1);
        Bar bar2 = new Bar();
        bar2.value = "SECOND";
        bar2.id = 2;
        foo.secondBar.add(bar2);
        String string = xmlMapper.writeValueAsString(foo);
        Foo fooRead = xmlMapper.readValue(string, Foo.class);
        assertEquals(foo.secondBar.get(0).id, fooRead.secondBar.get(0).id);
    }
}
