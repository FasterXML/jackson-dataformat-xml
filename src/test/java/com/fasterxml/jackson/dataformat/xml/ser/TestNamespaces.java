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
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    // [Issue-26]: should prefer the "default namespace"
    public void testRootNamespace() throws Exception
    {
        Person person = new Person();
        person.setName( "hello" );
        
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(person);

        // should use "the default namespace"...
        final String PREFIX = "<person xmlns=";
        if (!xml.startsWith(PREFIX)) {
            fail("Expected XML to begin with '"+PREFIX+"', instead got: "+xml);
        }
    }
}
