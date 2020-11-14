package com.fasterxml.jackson.dataformat.xml.failing;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class JaxbXmlValue418Test extends XmlTestBase
{
    // [dataformat-xml#418]
    @XmlRootElement(name = "ROOT")
    static class RootWithJaxbAnnotations {
        @XmlAttribute(name = "CHILD")
        JaxbChild child = new JaxbChild();
    }

    static class JaxbChild {
        @XmlAttribute
        String attr = "attr_value";

        @XmlValue
        String el = "text";

        public String getAttr() {
            return this.attr;
        }

        public String getEl() {
            return this.el;
        }

        public void setAttr(String attr) {
            this.attr = attr;
        }

        public void setEl(String el) {
            this.el = el;
        }
    }

    @JacksonXmlRootElement(localName = "ROOT")
    public static class RootWithJacksonAnnotations {
        @JacksonXmlProperty(localName = "CHILD")
        JacksonChild child = new JacksonChild();
    }

    public static class JacksonChild {
        @JacksonXmlProperty(isAttribute = true)
        String attr = "attr_value";

        @JacksonXmlText
        String el = "text";

        public String getAttr() {
            return this.attr;
        }

        public String getEl() {
            return this.el;
        }

        public void setAttr(String attr) {
            this.attr = attr;
        }

        public void setEl(String el) {
            this.el = el;
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper VANILLA_MAPPER = newMapper();

    private static final String EXPECTED_418 = "<ROOT><CHILD attr=\"attr_value\">text</CHILD></ROOT>";

    // [dataformat-xml#418]
    public void testWithJaxbAnnotations() throws Exception {
        final RootWithJaxbAnnotations value = new RootWithJaxbAnnotations();
        final XmlMapper mapper = XmlMapper.builder()
                .addModule(new com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule())
                .build();
        assertEquals(EXPECTED_418, mapper.writeValueAsString(value));
    }

    public void testWithJacksonAnnotations() throws Exception {
        final RootWithJacksonAnnotations value = new RootWithJacksonAnnotations();

        assertEquals(EXPECTED_418, VANILLA_MAPPER.writeValueAsString(value));
    }
}
