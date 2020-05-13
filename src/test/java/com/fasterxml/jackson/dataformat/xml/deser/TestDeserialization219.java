package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestDeserialization219 extends XmlTestBase
{
    // [dataformat-xml#219]
    static class Worker219
    {
        @JacksonXmlProperty(localName = "developer")
        String developer;
        @JacksonXmlProperty(localName = "tester")
        String tester;
        @JacksonXmlProperty(localName = "manager")
        String manager;
    }

    // [dataformat-xml#219]
    @JacksonXmlRootElement(localName="line")
    static class Line219 {
        public String code; //This should ideally be complex type
        public String amount;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#219]
    public void testWithAttribute219Worker() throws Exception
    {
        final String DOC =
"<worker>\n" + 
"  <developer>test1</developer>\n" + 
"  <tester grade='senior'>test2</tester>\n" + 
"  <manager>test3</manager>\n" + 
"</worker>"
                ;
        Worker219 result = MAPPER.readValue(DOC, Worker219.class);
        assertNotNull(result);
        assertEquals("test3", result.manager);
    }

    // [dataformat-xml#219]
    public void testWithAttribute219Line() throws Exception
    {
        final String DOC =
"<line>\n" + 
"    <code type='ABC'>qsd</code>\n" + 
"    <amount>138</amount>\n" + 
"</line>"
                ;
        Line219 result = MAPPER.readValue(DOC, Line219.class);
        assertNotNull(result);
        assertEquals("138", result.amount);
    }
}
