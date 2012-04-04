package com.fasterxml.jackson.dataformat.xml;

import java.io.IOException;

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
		public int a = 13;

		@JacksonXmlText
		public String text = "something";
	}
	
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testSimpleSerialize() throws IOException
    {
    	XmlMapper mapper = new XmlMapper();
    	String xml = mapper.writeValueAsString(new Simple());
    	assertEquals("<Simple a=\"13\">something</Simple>", xml);
    }

}
