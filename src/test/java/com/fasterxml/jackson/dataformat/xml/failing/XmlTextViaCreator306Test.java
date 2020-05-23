package com.fasterxml.jackson.dataformat.xml.failing;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class XmlTextViaCreator306Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "ROOT")
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

    @JacksonXmlRootElement(localName = "ROOT")
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

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

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
}
