package com.fasterxml.jackson.dataformat.xml.deser.creator;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.fail;

public class PojoWithCreatorRequired538Test extends XmlTestUtil
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
    @Test
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
