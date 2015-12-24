package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class EmptyElement167Test extends XmlTestBase
{
    static class A {
        public String d;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
    public void testIssue167() throws Exception
    {
        final String XML =
"<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n"+
"<d xsi:nil='true'/>\n"+
"</a>\n";
        A result = MAPPER.readValue(XML, A.class);
        assertNotNull(result);
    }
}
