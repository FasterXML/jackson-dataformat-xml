package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.fail;

public class DeserErrorHandling236Test extends XmlTestUtil
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
    @Test
    public void testExceptionWrapping() throws Exception
    {
        final String XML = "<name>monica&</name>";
        try {
            MAPPER.readValue(XML, Employee.class);
        } catch (StreamReadException e) {
            verifyException(e, "Unexpected character");
        } catch (Exception e) {
            fail("Wrong exception: "+e);
        }
    }
}
