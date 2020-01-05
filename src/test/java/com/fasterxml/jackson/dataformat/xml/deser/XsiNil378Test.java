package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNil378Test extends XmlTestBase
{
    private final static String XSI_NS_DECL = "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'";

    protected static class StringPair {
        public String first, second;
    }

    private final XmlMapper MAPPER = newMapper();

    // 04-Jan-2019, tatu: Due to default "Map Strings to Null values" coercion (see XmlMapper),
    //    tests for `null` handling are not quite right for 3.x... ideally would probably
    //    NOT use coercion, maybe, or (if possible) avoid doing that for `xsi:nil` induced
    //    reliable one. But as of now not 100% clear how this could be done so comment out tests
    
    // [dataformat-xml#378]
    public void testWithStringAsNull() throws Exception
    {
        StringPair bean;
        
        bean = MAPPER.readValue(
"<StringPair "+XSI_NS_DECL+"><first>not null</first><second xsi:nil='true' /></StringPair>",
            StringPair.class);
        assertNotNull(bean);

        assertEquals("not null", bean.first);

        // for 3.0:
//        assertNull(bean.second);
        assertEquals("", bean.second);
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
        assertEquals("not null", bean.second);

        // for 3.0:
//      assertNull(bean.first);
      assertEquals("", bean.first);
    }
}
