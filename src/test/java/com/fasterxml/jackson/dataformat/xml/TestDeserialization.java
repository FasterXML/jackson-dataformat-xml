package com.fasterxml.jackson.dataformat.xml;

import java.util.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

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

    static class ListBean
    {
        public List<Integer> values;
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
    /**
     * Unit test to ensure that we can succesfully also roundtrip
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

    public void testListBean() throws Exception
    {
        ListBean bean = MAPPER.readValue(
                "<ListBean><values><values>1</values><values>2</values><values>3</values></values></ListBean>",
                ListBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(3, bean.values.size());
        assertEquals(Integer.valueOf(1), bean.values.get(0));
        assertEquals(Integer.valueOf(2), bean.values.get(1));
        assertEquals(Integer.valueOf(3), bean.values.get(2));
    }

    // Issue#14:
    public void testMapWithAttr() throws Exception
    {
    	final String xml = "<order><person lang='en'>John Smith</person></order>";

    	/*
    	JsonParser jp = MAPPER.getJsonFactory().createJsonParser(xml);
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
}
