package com.fasterxml.jackson.dataformat.xml.deser.creator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.xml.*;

public class PojoWithCreatorRequired538Test extends XmlTestBase
{
    @JsonRootName(value = "bar")
    static class Bar538
    {
        int foo;

        public Bar538() { }

        @JsonCreator
        public Bar538(@JsonProperty(value = "foo", required = true) final int foo)
        {
            this.foo = foo;
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = mapperBuilder()
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .build();

    // [dataformat-xml#538]
    public void testPojoWithRequiredFromEmpty() throws Exception
    {
        // Should fail
        try {
            MAPPER.readValue("<bar></bar>", Bar538.class);
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Missing required creator property 'foo'");
        }
    }
}
