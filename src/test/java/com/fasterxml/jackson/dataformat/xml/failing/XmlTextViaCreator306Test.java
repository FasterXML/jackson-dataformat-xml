package com.fasterxml.jackson.dataformat.xml.failing;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

// [dataformat-xml#306]: Problem is that `@XmlText` has no nominal property name
// of empty String (""), and that is not properly bound. Worse, empty String has
// special meaning so that annotation CANNOT specify it, either.
public class XmlTextViaCreator306Test extends XmlTestBase
{
    // [dataformat-xml#306]
    @JsonRootName("ROOT")
    static class Root {
        @JacksonXmlProperty(localName = "CHILD")
        final Child child;

        public Root(Child child) {
            this.child = child;
        }

        public Child getChild() {
            return child;
        }
    }

    static class Child {
        @JacksonXmlProperty(localName = "attr", isAttribute = true)
        String attr;

        @JacksonXmlText
        String el;

        @ConstructorProperties({"attr", "el"})
        Child(String attr, String el) {
            this.attr = attr;
            this.el = el;
        }
    }

    @JsonRootName("ROOT")
    static class RootWithoutConstructor {
        @JacksonXmlProperty(localName = "CHILD")
        final ChildWithoutConstructor child;

        @ConstructorProperties({"child"})
        public RootWithoutConstructor(ChildWithoutConstructor child) {
            this.child = child;
        }

        public ChildWithoutConstructor getChild() {
            return child;
        }
    }

    static class ChildWithoutConstructor {
        @JacksonXmlProperty(isAttribute = true)
        public String attr;

        @JacksonXmlText
        public String el;
    }

    // [dataformat-xml#423]
    static class Sample423
    {
        final String text;
        final String attribute;

        @JsonCreator
        public Sample423(@JacksonXmlText String text,
                @JacksonXmlProperty(localName = "attribute", isAttribute = true)
                String attribute) {
            this.text = text;
            this.attribute = attribute;
        }
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#306]
    public void testIssue306WithCtor() throws Exception
    {
        final String XML = "<ROOT><CHILD attr='attr_value'>text</CHILD></ROOT>";
        Root root = MAPPER.readValue(XML, Root.class);
        assertNotNull(root);
    }

    public void testIssue306NoCtor() throws Exception
    {
        final String XML = "<ROOT><CHILD attr='attr_value'>text</CHILD></ROOT>";
        RootWithoutConstructor rootNoCtor = MAPPER.readValue(XML, RootWithoutConstructor.class);
        assertNotNull(rootNoCtor);
    }

    // [dataformat-xml#423]
    public void testXmlTextViaCtor423() throws Exception
    {
        final String XML = "<Sample423 attribute='attrValue'>text value</Sample423>";
        Sample423 result = MAPPER.readValue(XML, Sample423.class);
        assertEquals("attrValue", result.attribute);
        assertEquals("text value", result.text);
    }
}
