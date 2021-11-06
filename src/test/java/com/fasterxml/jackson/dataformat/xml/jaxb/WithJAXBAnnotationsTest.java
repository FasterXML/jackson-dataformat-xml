package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.io.IOException;

import jakarta.xml.bind.annotation.*;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Although XML-backed data binding does not rely (or directly build) on JAXB
 * annotations, it should be possible to use them similar to how they are used
 * with default Jackson JSON data binding. Let's verify this is the case.
 */
public class WithJAXBAnnotationsTest extends XmlTestBase
{
    /*
    /**********************************************************************
    /* Helper types
    /**********************************************************************
     */

    @XmlRootElement(name="bean")
    public static class RootBean
    {
        public String value = "text";
    }
    
    public static class AttrBean
    {        
        @XmlAttribute
        public String attr = "3";
    }

    @jakarta.xml.bind.annotation.XmlRootElement(name="Simple")
    static class WithXmlValue
    {
        @jakarta.xml.bind.annotation.XmlAttribute
        public int a = 13;

        @jakarta.xml.bind.annotation.XmlValue
        public String text = "something";
    }

    @XmlRootElement(name = "Individual")
    @JsonPropertyOrder({ "id", "firstName", "lastName"})
    static class MyPerson {
        @XmlAttribute(name = "identifier")
        public Long id;

        @XmlElement(name = "givenName")
        public String firstName;
    
        @XmlElement(name = "surName")
        public String lastName;

        public Long getId() {
            return id;
        }
        public void setId(final Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }
        public void setLastName(final String lastName) {
            this.lastName = lastName;
        }
    }

    /*
    /**********************************************************************
    /* Set up
    /**********************************************************************
     */

    protected XmlMapper _jaxbMapper;
    protected XmlMapper _nonJaxbMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _jaxbMapper = new XmlMapper();
        _nonJaxbMapper = new XmlMapper();
        // Use JAXB-then-Jackson annotation introspector
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance
                (jakartaXMLBindAnnotationIntrospector(), new JacksonAnnotationIntrospector());
        _jaxbMapper.setAnnotationIntrospector(intr);
    }

    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    /**
     * Unit test for verifying that root element name can be overridden
     * with {@link XmlRootElement} annotation.
     */
    public void testRootName() throws Exception
    {
        RootBean bean = new RootBean();
        // without JAXB annotations will use class simple name:
        assertEquals("<RootBean><value>text</value></RootBean>", _nonJaxbMapper.writeValueAsString(bean));
        assertEquals("<bean><value>text</value></bean>", _jaxbMapper.writeValueAsString(bean));
    }

    /**
     * Unit test for verifying that a property defaults to being written as
     * element, but can be redefined with {@link XmlAttribute} annotation.
     */
    public void testSerializeAsAttr() throws Exception
    {
        AttrBean bean = new AttrBean();
        assertEquals("<AttrBean><attr>3</attr></AttrBean>", _nonJaxbMapper.writeValueAsString(bean));
        assertEquals("<AttrBean attr=\"3\"/>", _jaxbMapper.writeValueAsString(bean));
    }

    /**
     * Unit test for verifying correct handling of
     * {@link XmlValue} annotation.
     */
    public void testAsTextWithJAXB() throws IOException
    {
    	// first: serialize
    	String xml = _jaxbMapper.writeValueAsString(new WithXmlValue());
    	assertEquals("<Simple a=\"13\">something</Simple>", xml);

    	// and then deserialize back...
    	WithXmlValue result = _jaxbMapper.readValue("<Simple a='99'>else</Simple>",
    			WithXmlValue.class);
    	assertEquals(99, result.a);
    	assertEquals("else", result.text);
    }

    public void testPersonAsXml() throws Exception {
        MyPerson person = new MyPerson();
        person.id = Long.valueOf(1L);
        person.firstName = "Jay";
        person.lastName = "Unit";
    
        String json = _jaxbMapper.writeValueAsString(person);
// System.out.println("Person: " + json);
    
        String expected = "<Individual identifier=\"1\"><givenName>Jay</givenName>"
                +"<surName>Unit</surName></Individual>";
        assertEquals(expected, json);
    }
}
