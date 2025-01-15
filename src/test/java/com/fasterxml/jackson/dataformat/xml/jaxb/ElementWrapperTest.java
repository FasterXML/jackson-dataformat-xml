package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import com.fasterxml.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class ElementWrapperTest extends XmlTestUtil
{
    @XmlRootElement(name = "Individual")
    static class MyPerson {
          public String name;

          @XmlElementWrapper(name = "offspring")
          @XmlElement(name = "kid")
          public List<MyPerson> children = new ArrayList<MyPerson>();
    }

    @XmlRootElement(name="p")
    static class MyPerson2 {
        public String name;

        public List<MyPerson2> child = new ArrayList<MyPerson2>();
    }
    
    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    @Test
    public void testElementWrapper() throws Exception
    {
        XmlMapper _jaxbMapper = new XmlMapper();
        // Use JAXB-then-Jackson annotation introspector
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance
            (jakartaXMLBindAnnotationIntrospector(),
                    new JacksonAnnotationIntrospector());
        _jaxbMapper.setAnnotationIntrospector(intr);

        MyPerson person = new MyPerson();
        person.name = "Jay";

        MyPerson child = new MyPerson();
        child.name = "Junior";
        
        person.children.add(child);

        String xml = _jaxbMapper.writer().writeValueAsString(person);
        
        String expected = "<Individual><name>Jay</name>"
                + "<offspring><kid><name>Junior</name><offspring/></kid></offspring></Individual>";
        assertEquals(expected, xml);
    }

    // And with JAXB, default should be "no wrapper"
    @Test
    public void testNoElementWrapper() throws Exception
    {
        XmlMapper jaxbMapper = mapperBuilder()
                .annotationIntrospector(jakartaXMLBindAnnotationIntrospector())
                .build();

        MyPerson2 person = new MyPerson2();
        person.name = "Jay";

        MyPerson2 child = new MyPerson2();
        child.name = "Junior";
        
        person.child.add(child);

        String xml = jaxbMapper.writeValueAsString(person);
        
        boolean expected1 = xml.equals("<p><name>Jay</name><child><name>Junior</name></child></p>");
        boolean expected2 = xml.equals("<p><child><name>Junior</name></child><name>Jay</name></p>");
        assertTrue(expected1 || expected2);
    }
}
