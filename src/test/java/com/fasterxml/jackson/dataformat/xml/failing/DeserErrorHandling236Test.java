package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.*;

public class DeserErrorHandling236Test extends XmlTestBase
{
    static class Employee {
        public String name;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    protected XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#236]
    public void testExceptionWrapping() throws Exception
    {
        final String XML = "<name>monica&</name>";
        try {
            MAPPER.readValue(XML, Employee.class);
        } catch (JsonProcessingException e) {
            verifyException(e, "foobar");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Wrong exception: "+e);
        }
    }
}
