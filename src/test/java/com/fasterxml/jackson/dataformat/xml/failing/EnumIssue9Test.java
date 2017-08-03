package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// related to [dataformat-xml#9] (and possibly others)
public class EnumIssue9Test extends XmlTestBase
{
    static enum TestEnum { A, B, C; }
    
    static class UntypedEnumBean
    {
       @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="__type")
// this would actually work:
//        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_OBJECT)
        public Object value;

        public UntypedEnumBean() { }
        public UntypedEnumBean(TestEnum v) { value = v; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
    public void testUntypedEnum() throws Exception
    {
        ObjectMapper mapper = new XmlMapper();
        String xml = mapper.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        
        UntypedEnumBean result = mapper.readValue(xml, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        
        if (TestEnum.class != ob.getClass()) {
            fail("Failed to deserialize TestEnum (got "+ob.getClass().getName()+") from: "+xml);
        }

        assertEquals(TestEnum.B, result.value);
    }

    // [dataformat-xml#121]
    public void testRootEnumIssue121() throws Exception
    {
        String xml = MAPPER.writeValueAsString(TestEnum.B);
        TestEnum result = MAPPER.readValue(xml, TestEnum.class);
        assertNotNull(result);
        assertEquals(TestEnum.B, result);
    }
}
