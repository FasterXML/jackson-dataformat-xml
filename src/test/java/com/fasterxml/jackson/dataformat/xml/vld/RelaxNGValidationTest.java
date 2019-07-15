package com.fasterxml.jackson.dataformat.xml.vld;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

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

    public void testValidWithNamespace() throws Exception
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

    public void testInvalidWithNamespace()
        throws Exception
    {
        XMLValidationSchema schema = parseRngSchema(SIMPLE_RNG_NS_SCHEMA);

        // First, wrong root element:
        String XML = "<root xmlns='http://test'>\n"
            +"<leaf />\n"
            +"</root>";
        verifyRngFailure(XML, schema, "wrong root element",
                         "namespace URI of tag \"root\" is wrong");

        // Wrong child namespace
        XML = "<root>\n"
            +"<leaf xmlns='http://other' />\n"
            +"</root>";
        verifyRngFailure(XML, schema, "wrong child element namespace",
                         "namespace URI of tag \"leaf\" is wrong.");

        // Wrong attribute namespace
        XML = "<root>\n"
            +"<ns:leaf xmlns:ns='http://test' ns:attr1='123' />\n"
            +"</root>";
        verifyRngFailure(XML, schema, "wrong attribute namespace",
                         "unexpected attribute \"attr1\"");
    }

    private void verifyRngFailure(String xml, XMLValidationSchema schema, String failMsg, String failPhrase)
        throws Exception
    {
        XMLStreamReader2 sr = getXMLReader(xml);
        sr.validateAgainst(schema);
        try {
            while (sr.hasNext()) {
                /*int type =*/ sr.next();
            }
            fail("Expected validity exception for "+failMsg);
        } catch (XMLValidationException vex) {
            String origMsg = vex.getMessage();
            String msg = (origMsg == null) ? "" : origMsg.toLowerCase();
            if (msg.indexOf(failPhrase.toLowerCase()) < 0) {
                String actualMsg = "Expected validation exception for "+failMsg+", containing phrase '"+failPhrase+"': got '"+origMsg+"'";
                fail(actualMsg);
            }
            // should get this specific type; not basic stream exception
        } catch (XMLStreamException sex) {
            fail("Expected XMLValidationException for "+failMsg);
        }
    }
}
