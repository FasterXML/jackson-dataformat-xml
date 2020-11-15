package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestNamespaces extends XmlTestBase
{
    final static String CHILD_NS = "uri:child";
    
    @JacksonXmlRootElement(localName="person", namespace="http://example.org/person" )
    static class Person
    {
        private String name;

        @JacksonXmlProperty(namespace = "http://example.org/personXML")
        public String getName() { return name; }
        public void setName(String name) {
            this.name = name;
        }
    }

    // New with 2.12: `@JsonProperty` allows use of "namespace" too; also
    // we test already existing (since 2.4) namespace support of
    // `@JsonRootName` as well
    @JsonRootName(value="person", namespace="http://example.org/person" )
    static class PersonWithRootName
    {
        private String name;

        @JsonProperty(namespace = "http://example.org/personJSON")
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

    @JsonRootName("Root")
    static class ChildWithNsXmlProp {
        @JacksonXmlProperty(namespace = CHILD_NS, localName = "ChildXML")
        public int child = 4;
    }

    @JsonRootName("Root")
    static class ChildWithNsJsonProp {
        @JsonProperty(namespace = CHILD_NS, value = "ChildJSON")
        public int child = 4;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#26]: should prefer the "default namespace"
    public void testRootNamespaceOlder() throws Exception
    {
        Person person = new Person();
        person.setName( "hello" );
        _verifyPerson(MAPPER, person);
    }

    // and a variant with `@JsonRootName`
    public void testRootNamespaceNewer() throws Exception
    {
        PersonWithRootName person = new PersonWithRootName();
        person.setName( "hello" );
        _verifyPerson(MAPPER, person);
    }

    private void _verifyPerson(XmlMapper mapper, Object value) throws Exception
    {
        String xml = MAPPER.writeValueAsString(value);

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

    public void testXmlNamespaceWithXmlProp() throws Exception {
        _verifyChild(MAPPER, new ChildWithNsXmlProp());
    }

    // Jackson 2.12 allows "namespace" with `@JsonProperty` too; verify
    public void testXmlNamespaceWithJsonProp() throws Exception {
        _verifyChild(MAPPER, new ChildWithNsJsonProp());
    }

    private void _verifyChild(XmlMapper mapper, Object value) throws Exception
    {
        String xml = MAPPER.writeValueAsString(value);
        final String PREFIX = "<Root><";
        if (!xml.startsWith(PREFIX)) {
            fail("Expected XML to begin with '"+PREFIX+"', instead got: "+xml);
        }
        // but that's just a prereq... need actual namespace declaration next
        String rest = xml.substring(PREFIX.length()).trim();
        rest = rest.substring(0, rest.indexOf('>'));
        if (!rest.contains(CHILD_NS)) {
            fail("Expected declaration for namespace '"+CHILD_NS
                    +"' in XML; do not see one in fragment: ["+rest+"]");
        }
    }
}
