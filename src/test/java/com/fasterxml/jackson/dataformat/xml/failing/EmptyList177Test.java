package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

// for [dataformat-xml#177]
public class EmptyList177Test extends XmlTestBase
{
    static class Config
    {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
        
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Entry> entry;
    }

    static class Entry
    {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
    public void testEmptyList() throws Exception
    {
        Config r = MAPPER.readValue(
                "<Config id='123'>\n"+
                "  <entry id='foo'> </entry>\n"+
                "</Config>\n",
                Config.class);
        assertNotNull(r);
    }
}
