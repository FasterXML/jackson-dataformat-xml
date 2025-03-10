package com.fasterxml.jackson.dataformat.xml.tofix;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// For [dataformat-xml#27]
public class ConflictingGetters27Test extends XmlTestUtil
{
    // [dataformat-xml#27]
    @JsonRootName("output")
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
    /* Test methods
    /**********************************************************************
     */

    // [dataformat-xml#27]
    @JacksonTestFailureExpected
    @Test
    public void testIssue27() throws Exception
    {
        XmlMapper mapper = new XmlMapper();

        Bean bean = new Bean();
        BeanInfo beanInfo = new BeanInfo("name");
        BeanInfo beanOther = new BeanInfo("name");
        bean.setBeanInfo(new BeanInfo[] { beanInfo });
        bean.setBeanOther(new BeanInfo[] { beanOther });

        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
//        System.out.println(output);
    }
}
