package com.fasterxml.jackson.dataformat.xml.vld;

import javax.xml.stream.XMLStreamConstants;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationException;
import org.codehaus.stax2.validation.XMLValidationSchema;

public class RelaxNGValidationTest extends ValidationTestBase
{
    final static String SIMPLE_RNG_NS_SCHEMA =
            "<element xmlns='http://relaxng.org/ns/structure/1.0' name='root'>\n"
            +" <zeroOrMore>\n"
            +"  <element name='ns:leaf' xmlns:ns='http://test'>\n"
            +"   <optional>\n"
            +"     <attribute name='attr1' />\n"
            +"   </optional>\n"
            +"   <optional>\n"
            +"     <attribute name='ns:attr2' />\n"
            +"   </optional>\n"
            +"   <text />\n"
            +"  </element>\n"
            +" </zeroOrMore>\n"
            +"</element>"
    ;

    public void testSimpleNs() throws Exception
    {
        String XML = "<root>\n"
            +" <myns:leaf xmlns:myns='http://test' attr1='123' />\n"
            +" <ns2:leaf xmlns:ns2='http://test' ns2:attr2='123' />\n"
            +"</root>"
            ;

        XMLValidationSchema schema = parseRngSchema(SIMPLE_RNG_NS_SCHEMA);
        XMLStreamReader2 sr = getXMLReader(XML);
        sr.validateAgainst(schema);

        try {
            assertTokenType(XMLStreamConstants.START_ELEMENT, sr.next());
            assertEquals("root", sr.getLocalName());
            
            while (sr.hasNext()) {
                sr.next();
            }
        } catch (XMLValidationException vex) {
            fail("Did not expect validation exception, got: "+vex);
        }

        assertTokenType(XMLStreamConstants.END_DOCUMENT, sr.getEventType());
    }
}
