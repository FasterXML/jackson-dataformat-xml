package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

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
}
