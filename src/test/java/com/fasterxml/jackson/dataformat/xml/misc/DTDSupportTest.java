package com.fasterxml.jackson.dataformat.xml.misc;

import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.*;

// for [databind-xml#211]
public class DTDSupportTest extends XmlTestBase
{
    public void testDTDAttempt() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        String XML = "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE foo SYSTEM 'http://127.0.0.1:8001' [ ]>\n"
                +"<foo/>";

        try {
            /*Map<String, String> info =*/ mapper.readValue(XML, Map.class);
            //At this point a GET request would have been sent to localhost:8001. You will see a Connection Refused in case you don't have a server listening there.
        } catch (Exception e){
            fail("Should not try to resolve external DTD subset: "+e);
        }
    }
}
