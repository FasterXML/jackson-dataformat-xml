package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TestStringValues162 extends XmlTestBase
{
    static class Name {
        public String first;
        public String last;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    public void testEmptyString162() throws Exception
    {
        Name name = MAPPER.readValue("<name><first>Ryan</first><last></last></name>",
                Name.class);
        assertNotNull(name);
        assertEquals("Ryan", name.first);
        assertEquals("", name.last);
    }
}
