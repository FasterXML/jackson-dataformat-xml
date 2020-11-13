package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class VerifyRootLocalName247Test extends XmlTestBase
{
    static class Root {
        public int value;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    public void testRootNameValidation247() throws Exception
    {
        Root root = MAPPER.readValue("<Root><value>42</value></Root>", Root.class);
        assertEquals(42, root.value);

        // so far so good. But why no validation for root local name?
        try {
            MAPPER.readValue("<Boot><value>42</value></Boot>", Root.class);
            fail("Should not allow wrong local name!");
        } catch (JsonProcessingException e) {
            verifyException(e, "Foobar");
        }
    }
}
