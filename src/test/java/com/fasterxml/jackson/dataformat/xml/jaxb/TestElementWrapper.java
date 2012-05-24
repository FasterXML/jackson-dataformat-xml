package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestElementWrapper extends XmlTestBase
{
    @XmlRootElement(name = "Individual")
    class MyPerson {
          public String name;

          @XmlElementWrapper(name = "offspring")
          @XmlElement(name = "kid")
          public List<MyPerson> children = new ArrayList<MyPerson>();
    }

    @JacksonXmlRootElement(localName = "output")
    static class Bean {
        public BeanInfo[] beanInfo;
        public BeanInfo[] beanOther;

        @JacksonXmlElementWrapper(localName = "beanInfo")
        @JacksonXmlProperty(localName = "item")
        public BeanInfo[] getBeanInfo() {
            return beanInfo;
        }

        public void setBeanInfo(BeanInfo[] beanInfo) {
            this.beanInfo = beanInfo;
        }

        @JacksonXmlElementWrapper(localName = "beanOther")
        @JacksonXmlProperty(localName = "item")
        public BeanInfo[] getBeanOther() {
            return beanOther;
        }

        public void setBeanOther(BeanInfo[] beanOther) {
            this.beanOther = beanOther;
        }
    }    
    static class BeanInfo {
        public String name;

        public BeanInfo() { }
        public BeanInfo(String n) { name = n; }
    }

    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    public void testElementWrapper() throws Exception
    {
        XmlMapper _jaxbMapper = new XmlMapper();
        // Use JAXB-then-Jackson annotation introspector
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance
            (new XmlJaxbAnnotationIntrospector(), new JacksonAnnotationIntrospector());
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

    public void testIssue27() throws Exception
    {
        XmlMapper mapper = new XmlMapper();

        Bean bean = new Bean();
        BeanInfo beanInfo = new BeanInfo("name");
        BeanInfo beanOther = new BeanInfo("name");
        bean.setBeanInfo(new BeanInfo[] { beanInfo });
        bean.setBeanOther(new BeanInfo[] { beanOther });
        String output = mapper.writeValueAsString(bean);
        System.out.println(output);
    }
}
