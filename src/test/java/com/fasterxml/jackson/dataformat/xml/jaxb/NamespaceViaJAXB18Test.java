package com.fasterxml.jackson.dataformat.xml.jaxb;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

public class NamespaceViaJAXB18Test extends XmlTestBase
{
    final static String TEST_NAMESPACE = "http://namespace-base";

    @XmlRootElement(namespace = TEST_NAMESPACE)
    interface Facility { }

    static class House implements Facility {
        @XmlAttribute
        public String getName() { return "Bob"; }
    }

    @XmlRootElement(namespace = "", name = "Houzz")
    static class HouseWithNoNamespace implements Facility {
        @XmlAttribute
        public String getName() { return "Bill"; }
    }

    // Also: for better or worse, cannot only override local name so:
    @XmlRootElement(name = "Houzz2")
    static class HouseWithNoNamespace2 implements Facility {
        @XmlAttribute
        public String getName() { return "Frank"; }
    }
    
    private final XmlMapper MAPPER = mapperBuilder()
            .annotationIntrospector(jakartaXMLBindAnnotationIntrospector())
            .build();

    // [dataformat-xml#18]
    public void testNamespaceViaJAXB() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new House());
        if (!xml.contains("<House xmlns")
                 || !xml.contains(TEST_NAMESPACE)) {
            fail("Expected `xmlns` declaration for element `House`, none seen: XML = "+xml);
        }

        // But should be able to mask it...
        xml = MAPPER.writeValueAsString(new HouseWithNoNamespace());
        if (!xml.contains("<Houzz")
                || xml.contains("xmlns")) {
            fail("Expected NO `xmlns` declaration for element `Houzz` but got XML = "+xml);
        }

        xml = MAPPER.writeValueAsString(new HouseWithNoNamespace2());
        if (!xml.contains("<Houzz2")
                || xml.contains("xmlns")) {
            fail("Expected NO `xmlns` declaration for element `Houzz2` but got XML = "+xml);
        }
    }
}
