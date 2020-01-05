package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNilForStringsTest extends XmlTestBase
{
    private final static String XSI_NS_DECL = "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'";

    protected static class StringPair {
        public String first, second;
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#378]
    public void testWithStringAsNull() throws Exception
    {
        StringPair bean;
        
        bean = MAPPER.readValue(
"<StringPair "+XSI_NS_DECL+"><first>not null</first><second xsi:nil='true' /></StringPair>",
            StringPair.class);
        assertNotNull(bean);
        assertEquals("not null", bean.first);
        assertNull(bean.second);
    }

    // [dataformat-xml#378]
    public void testWithStringAsNull2() throws Exception
    {
        StringPair bean;
        
        bean = MAPPER.readValue(
"<StringPair "+XSI_NS_DECL+"><first xsi:nil='true' /><second>not null</second></StringPair>",
//"<StringPair "+XSI_NS_DECL+"><first xsi:nil='true'></first><second>not null</second></StringPair>",
            StringPair.class);
        assertNotNull(bean);
        assertNull(bean.first);
        assertEquals("not null", bean.second);
    }
}
