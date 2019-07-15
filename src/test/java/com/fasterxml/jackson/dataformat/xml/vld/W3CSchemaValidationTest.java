package com.fasterxml.jackson.dataformat.xml.vld;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationException;
import org.codehaus.stax2.validation.XMLValidationSchema;

import static javax.xml.stream.XMLStreamConstants.*;

// Basic verification that W3C Schema validation is available
public class W3CSchemaValidationTest extends ValidationTestBase
{
    public void testSimpleDataTypes() throws Exception
    {
        // Another sample schema, from
        String SCHEMA = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n"
            + "<xs:element name='item'>\n"
            + " <xs:complexType>\n"
            + "  <xs:sequence>\n"
            + "   <xs:element name='quantity' type='xs:positiveInteger'/>"
            + "   <xs:element name='price' type='xs:decimal'/>"
            + "  </xs:sequence>"
            + " </xs:complexType>"
            + "</xs:element>"
            + "</xs:schema>";
        
        XMLValidationSchema schema = parseW3CSchema(SCHEMA);
        
        // First, valid doc:
        String XML = "<item><quantity>3  </quantity><price>\r\n4.05</price></item>";
        XMLStreamReader2 sr = getXMLReader(XML);
        sr.validateAgainst(schema);
        
        try {
            assertTokenType(START_ELEMENT, sr.next());
            assertEquals("item", sr.getLocalName());
            
            assertTokenType(START_ELEMENT, sr.next());
            assertEquals("quantity", sr.getLocalName());
            String str = sr.getElementText();
            assertEquals("3", str.trim());
            
            assertTokenType(START_ELEMENT, sr.next());
            assertEquals("price", sr.getLocalName());
            str = sr.getElementText();
            assertEquals("4.05", str.trim());
            
            assertTokenType(END_ELEMENT, sr.next());
            assertTokenType(END_DOCUMENT, sr.next());
        } catch (XMLValidationException vex) {
            fail("Did not expect validation exception, got: " + vex);
        }
        sr.close();
        
        // Then invalid (wrong type for value)
        XML = "<item><quantity>34b</quantity><price>1.00</price></item>";
        sr.validateAgainst(schema);
        verifyFailure(XML, schema, "invalid 'positive integer' datatype",
                      "does not satisfy the \"positiveInteger\"");
        sr.close();
    }
}
