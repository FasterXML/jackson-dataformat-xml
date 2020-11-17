package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class VerifyRootLocalName247Test extends XmlTestBase
{
    // [dataformat-xml#247]
    static class Root {
        public int value;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#247]
    public void testRootNameValidation247() throws Exception
    {
        Root root = MAPPER
                .readerFor(Root.class)
// 16-Nov-2020, tatu: no time to implement for 2.12, hopefully next version?
//                .with(FromXmlParser.Feature.ENFORCE_VALID_ROOT_NAME)
                .readValue("<Root><value>42</value></Root>");
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
