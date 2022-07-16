package tools.jackson.dataformat.xml.deser;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.dataformat.xml.*;

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
        } catch (StreamReadException e) {
            verifyException(e, "Unexpected character");
        } catch (Exception e) {
            fail("Wrong exception: "+e);
        }
    }
}
