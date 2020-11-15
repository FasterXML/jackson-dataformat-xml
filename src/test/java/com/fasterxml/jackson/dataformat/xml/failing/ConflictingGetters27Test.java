package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ConflictingGetters27Test extends XmlTestBase
{
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
    /* Unit tests
    /**********************************************************************
     */

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
