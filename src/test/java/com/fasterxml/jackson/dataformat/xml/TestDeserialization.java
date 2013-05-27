package com.fasterxml.jackson.dataformat.xml;

import java.util.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class TestDeserialization extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

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

    static class Optionals {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Optional> optional;
    } 

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
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
    
    // Issue#14:
    public void testMapWithAttr() throws Exception
    {
    	final String xml = "<order><person lang='en'>John Smith</person></order>";

    	/*
    	JsonParser jp = MAPPER.getJsonFactory().createParser(xml);
    	JsonToken t;
    	while ((t = jp.nextToken()) != null) {
    		switch (t) {
    		case FIELD_NAME:
    			System.out.println("Field '"+jp.getCurrentName()+"'");
    			break;
    		case VALUE_STRING:
    			System.out.println("text '"+jp.getText()+"'");
    		default:
    			System.out.println("Token "+t);
    		}
    	}
    	*/
    	
    	Map<?,?> map = MAPPER.readValue(xml, Map.class);
    	
    	// Will result in equivalent of:
    	// { "person" : {
    	//     "lang" : "en",
    	//     "" : "John Smith"
    	//   }
    	// }
    	//
    	// which may or may not be what we want. Without attribute
    	// we would just have '{ "person" : "John Smith" }'
    	
    	assertNotNull(map);
    }

    // // Tests for [Issue#64]

    public void testOptionalAttr() throws Exception
    {
        Optional ob = MAPPER.readValue("<phone type='work'>123-456-7890</phone>",
                Optional.class);
        assertNotNull(ob);
        assertEquals("123-456-7890", ob.number);
        assertEquals("work", ob.type);
    }

    public void testMissingOptionalAttr() throws Exception
    {
        Optional ob = MAPPER.readValue("<phone>123-456-7890</phone>",
                Optional.class);
        assertNotNull(ob);
                System.err.println("ob: " + ob);
        assertEquals("123-456-7890", ob.number);
        assertEquals("NOT SET", ob.type);
    }

    public void testMultiOptional() throws Exception
    {
        Optionals ob = MAPPER.readValue("<MultiOptional><optional type='work'>123-456-7890</optional></MultiOptional>",
                Optionals.class);
        assertNotNull(ob);
        assertNotNull(ob.optional);
        assertEquals(1, ob.optional.size());
//        System.err.println("ob: " + ob); // works fine
        Optional opt = ob.optional.get(0);
        assertEquals("123-456-7890", opt.number);
        assertEquals("work", opt.type);
    }
    
        public void testMultiOptionalWithMissingType() throws Exception
    {
            Optionals ob = MAPPER.readValue("<MultiOptional><optional>123-456-7890</optional></MultiOptional>",
                    Optionals.class);
            assertNotNull(ob);
            assertNotNull(ob.optional);
            assertEquals(1, ob.optional.size());
//            System.err.println("ob: " + ob); // works fine
            Optional opt = ob.optional.get(0);
            assertEquals("123-456-7890", opt.number);
            assertEquals("NOT SET", opt.type);
    }
}
