package com.fasterxml.jackson.dataformat.xml.jaxb;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

// Problem with handling of `@XmlValue` via JAXBAnnotationIntrospector
// is that by default it gives implicit name of `value` for virtual
// property. Although accessor itself will be specially processed, this
// may prevent normal combining of getter/setter/field combo.
// To prevent issues it is necessary to use one of work-arounds:
//
// 1. Annotate all relevant accessors, not just one (since implicit name
//   binding can not be relied on)
// 2. Override default implicit name to be `null`, which should allow
//   combination of accessors
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

        // NOTE! One work-around for issue would be to move this to `getEl()`
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

    @JsonRootName("ROOT")
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

        // 13-Nov-2020, tatu: 2 main ways to resolve the problem, either (a) move
        //    annotation to getter or (b) remove "implicit name" for `@XmlValue`.
        //    We'll do latter here:
        final XmlMapper mapper = XmlMapper.builder()
                .addModule(new com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule()
                        .setNameUsedForXmlValue(null)
                )
                .build();
        final String xml = mapper.writeValueAsString(value);
        assertEquals(EXPECTED_418, xml);
    }

    public void testWithJacksonAnnotations() throws Exception {
        final RootWithJacksonAnnotations value = new RootWithJacksonAnnotations();

        assertEquals(EXPECTED_418, VANILLA_MAPPER.writeValueAsString(value));
    }
}
