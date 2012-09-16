package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.io.IOException;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TestAttributes extends XmlTestBase
{
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public class Jurisdiction {
        @XmlAttribute(name="name",required=true)
        protected String name = "Foo";
        @XmlAttribute(name="value",required=true)
        protected int value = 13;
    }

    /*
    /**********************************************************************
    /* Set up
    /**********************************************************************
     */

    protected XmlMapper _jaxbMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _jaxbMapper = new XmlMapper();
        _jaxbMapper.setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testIssue6() throws IOException
    {
        assertEquals("<Jurisdiction name=\"Foo\" value=\"13\"/>",
                _jaxbMapper.writeValueAsString(new Jurisdiction()));
    }
}
