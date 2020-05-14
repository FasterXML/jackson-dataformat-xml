package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestNamespaces extends XmlTestBase
{
    @JacksonXmlRootElement(localName="person", namespace="http://example.org/person" )
    static class Person
    {
        private String name;

        @JacksonXmlProperty(namespace = "http://example.org/person")
        public String getName() { return name; }
        public void setName(String name) {
            this.name = name;
        }
    }

    static class Issue395 {
        @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/XML/1998/namespace",
                localName = "lang")
        public String lang = "en-US";
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#26]: should prefer the "default namespace"
    public void testRootNamespace() throws Exception
    {
        Person person = new Person();
        person.setName( "hello" );
        
        String xml = MAPPER.writeValueAsString(person);

        // should use "the default namespace"...
        final String PREFIX = "<person xmlns=";
        if (!xml.startsWith(PREFIX)) {
            fail("Expected XML to begin with '"+PREFIX+"', instead got: "+xml);
        }
    }

    // [dataformat-xml#395]: should not bind standard `xml` namespace to anything
    public void testXmlNs() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Issue395());
//        System.err.println("XML: "+xml);
        assertEquals("<Issue395 xml:lang=\"en-US\"/>", xml.trim());
    }
}
