package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.io.IOException;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class AttributesWithJAXBTest extends XmlTestBase
{
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public class Jurisdiction {
        @XmlAttribute(name="name",required=true)
        protected String name = "Foo";

        @XmlAttribute(name="value",required=true)
        protected int value = 13;
    }

    @XmlRootElement(name="problem")
    public static class Problem {
        @XmlAttribute(name="id")     
        public String id;
        public String description;

        public Problem() { }
        public Problem(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testTwoAttributes() throws IOException
    {
        XmlMapper mapper = new XmlMapper();
//        mapper.setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        String xml = mapper.writeValueAsString(new Jurisdiction());
        assertEquals("<Jurisdiction name=\"Foo\" value=\"13\"/>", xml);
    }

    public void testAttributeAndElement() throws IOException
    {
        XmlMapper mapper = new XmlMapper();
        mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        String xml = mapper.writeValueAsString(new Problem("x", "Stuff"));
        assertEquals("<problem id=\"x\"><description>Stuff</description></problem>", xml);
    }
}
