package com.fasterxml.jackson.xml;

import org.codehaus.jackson.annotate.JsonTypeInfo;

public class TestEnums extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    static enum TestEnum { A, B, C; }

    static class EnumBean
    {
        public TestEnum value;

        public EnumBean() { }
        public EnumBean(TestEnum v) { value = v; }
    }
    
    static class UntypedEnumBean
    {
        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="__type")
        public Object value;

        public UntypedEnumBean() { }
        public UntypedEnumBean(TestEnum v) { value = v; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testEnum() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        String str = mapper.writeValueAsString(new EnumBean(TestEnum.B));
        EnumBean result = mapper.readValue(str, EnumBean.class);
        assertNotNull(result);
        assertEquals(TestEnum.B, result.value);
    }

    public void testUntypedEnum() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        String str = mapper.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        UntypedEnumBean result = mapper.readValue(str, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        assertSame(TestEnum.class, ob.getClass());
        assertEquals(TestEnum.B, result.value);
    }
    
}
