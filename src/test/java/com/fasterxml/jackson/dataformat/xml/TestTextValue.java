package com.fasterxml.jackson.dataformat.xml;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class TestTextValue extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

	static class Simple
	{
		@JacksonXmlProperty(isAttribute=true)
		// same as: @javax.xml.bind.annotation.XmlAttribute
		public int a = 13;

		@JacksonXmlText
		// about same as: @javax.xml.bind.annotation.XmlValue
		public String text = "something";
	}
	
	// Issue-24:

	static class Main {
		@JsonProperty("com.test.stack") public Stack stack;
	}
	static class Stack {
		public String name;

		@JsonProperty("com.test.stack.slot")
		public Slot slot;
	}
	static class Slot {
		@JsonProperty("name")
		public String name;

		@JsonProperty("id")
		public String id;

		@JsonProperty("height")
		public String height;

		@JsonProperty("width")
		public String width;

		@JacksonXmlText
		public String value;
	}
	/*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testSerializeAsText() throws IOException
    {
    	XmlMapper mapper = new XmlMapper();
    	String xml = mapper.writeValueAsString(new Simple());
    	assertEquals("<Simple a=\"13\">something</Simple>", xml);
    }

    public void testDeserializeAsText() throws IOException
    {
    	XmlMapper mapper = new XmlMapper();
    	Simple result = mapper.readValue("<Simple a='99'>else</Simple>",
    			Simple.class);
    	assertEquals(99, result.a);
    	assertEquals("else", result.text);
    }
    
    public void testIssue24() throws Exception
    {
    	final String TEXT = "+/null/this is a long string";
    	final String XML =
    			"<main>\n"
    			+"<com.test.stack name='stack1'>\n"
    			+"<com.test.stack.slot height='0' id='0' name='slot0' width='0'>"
    			+TEXT
    			+"</com.test.stack.slot>\n"
    			+"</com.test.stack>\n"
    			+"</main>";
    	XmlMapper mapper = new XmlMapper();
    	Main main = mapper.readValue(XML, Main.class);
    	assertNotNull(main.stack);
    	assertNotNull(main.stack.slot);
    	assertEquals(TEXT, main.stack.slot.value);
    }
    
    /* // Uncomment to see how JAXB works here:
    public void testJAXB() throws Exception
    {
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.bind.JAXB.marshal(new Simple(), sw);
        System.out.println("JAXB -> "+sw);
    }
    */
}
