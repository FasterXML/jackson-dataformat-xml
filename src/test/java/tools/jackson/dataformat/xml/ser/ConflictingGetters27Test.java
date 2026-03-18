package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

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

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#27]: Serialization
    @Test
    public void testIssue27Serialize() throws Exception
    {
        Bean bean = new Bean();
        bean.setBeanInfo(new BeanInfo[] { new BeanInfo("name1") });
        bean.setBeanOther(new BeanInfo[] { new BeanInfo("name2") });

        String xml = MAPPER.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<beanInfo>"));
        assertTrue(xml.contains("<beanOther>"));
        assertTrue(xml.contains("<item>"));
    }

    // [dataformat-xml#27]: Roundtrip
    @Test
    public void testIssue27Roundtrip() throws Exception
    {
        Bean bean = new Bean();
        bean.setBeanInfo(new BeanInfo[] { new BeanInfo("name1") });
        bean.setBeanOther(new BeanInfo[] { new BeanInfo("name2") });

        String xml = MAPPER.writeValueAsString(bean);
        Bean result = MAPPER.readValue(xml, Bean.class);

        assertNotNull(result);
        assertNotNull(result.beanInfo);
        assertNotNull(result.beanOther);
        assertEquals(1, result.beanInfo.length);
        assertEquals(1, result.beanOther.length);
        assertEquals("name1", result.beanInfo[0].name);
        assertEquals("name2", result.beanOther[0].name);
    }
}
