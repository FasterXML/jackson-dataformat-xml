package com.fasterxml.jackson.dataformat.xml;

public class TestStringValues extends XmlTestBase
{
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
    public void testSimpleStringElement() throws Exception
    {
        // first, simple one to verify baseline
        StringBean bean = MAPPER.readValue("<StringBean><text>text!</text></StringBean>", StringBean.class);
        assertNotNull(bean);
        assertEquals("text!", bean.text);
    }
    
    public void testEmptyStringElement() throws Exception
    {
        // then with empty element
        StringBean bean = MAPPER.readValue("<StringBean><text></text></StringBean>", StringBean.class);
        assertNotNull(bean);
        // empty String or null?
        // 22-Sep-2012, tatu: Seems to be 'null', but should probably be fixed to ""
//        assertEquals("", bean.text);
        assertNull(bean.text);
    }
    
    public void testMissingString() throws Exception
    {
        StringBean baseline = new StringBean();
        // then missing
        StringBean bean = MAPPER.readValue("<StringBean />", StringBean.class);
        assertNotNull(bean);
        assertEquals(baseline.text, bean.text);
    }

    public void testStringWithAttribute() throws Exception
    {
    // and then the money shot: with 'standard' attribute...
        StringBean bean = MAPPER.readValue("<StringBean><text xml:lang='fi'>Pulla</text></StringBean>", StringBean.class);
        assertNotNull(bean);
        assertEquals("Pulla", bean.text);
    }
    
}
