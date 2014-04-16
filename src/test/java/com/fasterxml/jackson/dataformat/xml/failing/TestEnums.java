package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TestEnums extends XmlTestBase
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

    public void testUntypedEnum() throws Exception
    {
        ObjectMapper mapper = new XmlMapper();
        String xml = mapper.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        
        UntypedEnumBean result = mapper.readValue(xml, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        
        if (TestEnum.class != ob.getClass()) {
            fail("Failed to deserialize TestEnum (got "+ob.getClass()+getName()+") from: "+xml);
        }

        assertEquals(TestEnum.B, result.value);
    }
}
