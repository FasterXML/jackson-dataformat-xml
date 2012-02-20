package com.fasterxml.jackson.xml.jaxb;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import com.fasterxml.jackson.xml.XmlTestBase;

/**
 * Although XML-backed data binding does not rely (or directly build) on JAXB
 * annotations, it should be possible to use them similar to how they are used
 * with default Jackson JSON data binding. Let's verify this is the case.
 */
public class TestWithJAXBAnnotations extends XmlTestBase
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
            (new XmlJaxbAnnotationIntrospector(), new JacksonAnnotationIntrospector());
        _jaxbMapper.setAnnotationIntrospector(intr);
    }
    
    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    /**
     * Unit test for verifying that root element name can be overridden
     * with <code>@XmlRootElement</code> annotation.
     */
    public void testRootName() throws Exception
    {
        RootBean bean = new RootBean();
        // without JAXB annotations will use class simple name:
        assertEquals("<RootBean><value>text</value></RootBean>", _nonJaxbMapper.writeValueAsString(bean));
        assertEquals("<bean><value>text</value></bean>", _jaxbMapper.writeValueAsString(bean));
    }

    /**
     * Unit test for verifying that a propery defaults to being written as
     * element, but can be redefined with <code>@XmlAttribute</code> annotation.
     */
    public void testSerializeAsAttr() throws Exception
    {
        AttrBean bean = new AttrBean();
        assertEquals("<AttrBean><attr>3</attr></AttrBean>", _nonJaxbMapper.writeValueAsString(bean));
        assertEquals("<AttrBean attr=\"3\"/>", _jaxbMapper.writeValueAsString(bean));
    }
}
