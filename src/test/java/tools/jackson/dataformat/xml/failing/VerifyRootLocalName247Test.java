package tools.jackson.dataformat.xml.failing;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class VerifyRootLocalName247Test extends XmlTestUtil
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
    @Test
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
        } catch (DatabindException e) {
            verifyException(e, "Foobar");
        }
    }
}
