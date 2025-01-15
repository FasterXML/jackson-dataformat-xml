package com.fasterxml.jackson.dataformat.xml.vld;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.junit.jupiter.api.Test;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static org.junit.jupiter.api.Assertions.fail;

// Should test validation failure too but...
public class DTDValidationTest extends ValidationTestBase
{
    final static String SIMPLE_DTD =
        "<!ELEMENT root (leaf+)>\n"
        +"<!ATTLIST root attr CDATA #REQUIRED>\n"
        +"<!ELEMENT leaf EMPTY>\n"
        ;

    @Test
    public void testFullValidationOk() throws Exception
    {
        String XML = "<root attr='123'><leaf /></root>";
        XMLValidationSchema schema = parseDTDSchema(SIMPLE_DTD);
        XMLStreamReader2 sr = getXMLReader(XML);
        sr.validateAgainst(schema);
        while (sr.next() != END_DOCUMENT) { }
        sr.close();
    }

    @Test
    public void testValidationFail() throws Exception
    {
        String XML = "<root><leaf /></root>";
        XMLValidationSchema schema = parseDTDSchema(SIMPLE_DTD);
        XMLStreamReader2 sr = getXMLReader(XML);
        sr.validateAgainst(schema);

        try {
            sr.next();
            fail("Should not pass");
        } catch (XMLStreamException e) {
            verifyException(e, "Required attribute \"attr\" missing");
        }
        sr.close();
    }
}
