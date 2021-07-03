package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class ElementWrapperTest extends XmlTestBase
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

    public void testElementWrapper() throws Exception
    {
        // Use JAXB-then-Jackson annotation introspector
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance
            (new JaxbAnnotationIntrospector(),
                    new JacksonAnnotationIntrospector());
        XmlMapper jaxbMapper = XmlMapper.builder()
                .annotationIntrospector(intr)
                .build();

        MyPerson person = new MyPerson();
        person.name = "Jay";

        MyPerson child = new MyPerson();
        child.name = "Junior";
        
        person.children.add(child);

        String xml = jaxbMapper.writer().writeValueAsString(person);
        
        String expected = "<Individual><name>Jay</name>"
                + "<offspring><kid><name>Junior</name><offspring/></kid></offspring></Individual>";
        assertEquals(expected, xml);
    }

    // And with JAXB, default should be "no wrapper"
    public void testNoElementWrapper() throws Exception
    {
        XmlMapper jaxbMapper = mapperBuilder()
                .annotationIntrospector(new JaxbAnnotationIntrospector())
                .build();

        MyPerson2 person = new MyPerson2();
        person.name = "Jay";

        MyPerson2 child = new MyPerson2();
        child.name = "Junior";
        
        person.child.add(child);

        String xml = jaxbMapper.writeValueAsString(person);
        
        String expected = "<p><name>Jay</name><child><name>Junior</name></child></p>";
        assertEquals(expected, xml);
    }
}
