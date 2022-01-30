package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class TestDeserialization extends XmlTestBase
{
    static class AttributeBean
    {
        @JacksonXmlProperty(isAttribute=true, localName="attr")
        public String text = "?";
    }

    static class Optional {
        @JacksonXmlText
        public String number = "NOT SET";
        public String type = "NOT SET";
    }

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
    @JsonRootName("line")
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

    /**
     * Unit test to ensure that we can successfully also round trip
     * example Bean used in Jackson tutorial
     */
    public void testRoundTripWithJacksonExample() throws Exception
    {
        FiveMinuteUser user = new FiveMinuteUser("Joe", "Sixpack",
                true, FiveMinuteUser.Gender.MALE, new byte[] { 1, 2, 3 , 4, 5 });
        String xml = MAPPER.writeValueAsString(user);
        FiveMinuteUser result = MAPPER.readValue(xml, FiveMinuteUser.class);
        assertEquals(user, result);
    }

    public void testFromAttribute() throws Exception
    {
        AttributeBean bean = MAPPER.readValue("<AttributeBean attr=\"abc\"></AttributeBean>", AttributeBean.class);
        assertNotNull(bean);
        assertEquals("abc", bean.text);
    }

    // // Tests for [dataformat-xml#64]

    public void testOptionalAttr() throws Exception
    {
        Optional ob = MAPPER.readValue("<Optional type='work'>123-456-7890</Optional>",
                Optional.class);
        assertNotNull(ob);
        assertEquals("123-456-7890", ob.number);
        assertEquals("work", ob.type);
    }

    // 03-Jul-2020, tatu: Due to change on deserialization of root-level scalars,
    //    this test that passed on 2.11 is no longer valid (it wasn't even before
    //    wrt compatibility of serialization)
    /*
    public void testMissingOptionalAttr() throws Exception
    {
        Optional ob = MAPPER.readValue("<Optional>123-456-7890</Optional>",
                Optional.class);
        assertNotNull(ob);
        assertEquals("123-456-7890", ob.number);
        assertEquals("NOT SET", ob.type);
    }
    */

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
