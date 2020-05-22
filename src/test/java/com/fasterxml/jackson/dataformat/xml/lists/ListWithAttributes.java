package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class ListWithAttributes extends XmlTestBase
{
    // [dataformat-xml#43]
    static class Name {
        @JacksonXmlProperty(isAttribute=true)
        public String language;

        @JacksonXmlText
        public String text;

//        public String data;

        public Name() { }
    }

    static class RoomName {
        @JacksonXmlElementWrapper(localName = "names", useWrapping=true)
        @JsonProperty("name")
        public List<Name> names;
    }
    
    // [dataformat-xml#99]: allow skipping unknown properties
    static class Root {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "value")
        public List<Value> values;
    }

    public static class Value {
        @JacksonXmlProperty(isAttribute = true)
        public int id;
    }

    // [dataformat-xml#108]: unwrapped lists, more than one entry, id attributes
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

    // [dataformat-xml#301]: mismatched attribute to skip
    static class Parent301 {
        @JacksonXmlProperty(localName = "MY_ATTR", isAttribute = true)
        public String myAttribute;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "MY_PROPERTY")
        public List<ChildA301> childrenA = new ArrayList<>();

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "CHILDB")
        public List<ChildB301> childrenB = new ArrayList<>();

    }

    static class ChildA301 { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChildB301 {
        @JacksonXmlProperty(localName = "MY_PROPERTY")
        public Double value;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#43]
    public void testIssue43() throws Exception
    {
        String xmlData = "<roomName><names>"
                +"<name language=\"en\">SPECIAL</name>"
                +"</names></roomName>";

        RoomName roomName = MAPPER.readValue(xmlData, RoomName.class);
        assertEquals(1, roomName.names.size());
        assertEquals("SPECIAL", roomName.names.get(0).text);
    }
    
    // [dataformat-xml#99]: allow skipping unknown properties
    public void testListWithAttributes() throws Exception
    {
        String source = "<Root>"
                + "     <value id=\"1\"/>"
                + "     <fail/>"
                + "</Root>";
        ObjectReader r = MAPPER.readerFor(Root.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Root root = r.readValue(source, Root.class);
        assertNotNull(root.values);
        assertEquals(1, root.values.size());
    }
    
    // [dataformat-xml#108]: unwrapped lists, more than one entry, id attributes
    public void testIdsFromAttributes() throws Exception {
        Foo foo = new Foo();
        Bar bar1 = new Bar();
        bar1.id = 1;
        bar1.value = "FIRST";
        foo.firstBar.add(bar1);
        Bar bar2 = new Bar();
        bar2.value = "SECOND";
        bar2.id = 2;
        foo.secondBar.add(bar2);
        String string = MAPPER.writeValueAsString(foo);
        Foo fooRead = MAPPER.readValue(string, Foo.class);
        assertEquals(foo.secondBar.get(0).id, fooRead.secondBar.get(0).id);
    }

    public void testIssue301WithAttr() throws Exception {
        final String XML =
                "<PARENT>" +
                "  <CHILDB MY_ATTR='TEST_VALUE'>" +
                "    <MY_PROPERTY>12.25</MY_PROPERTY>" +
                "  </CHILDB>" +
                "</PARENT>";
        Parent301 result = MAPPER.readValue(XML, Parent301.class);
        assertNotNull(result);
        assertNotNull(result.childrenB);
        assertEquals(1, result.childrenB.size());
        assertEquals(Double.valueOf(12.25), result.childrenB.get(0).value);
    }
}

