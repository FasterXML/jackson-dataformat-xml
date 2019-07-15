package com.fasterxml.jackson.dataformat.xml.vld;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationException;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import static javax.xml.stream.XMLStreamConstants.*;

abstract class ValidationTestBase extends XmlTestBase
{
    final static HashMap<Integer,String> mTokenTypes = new HashMap<Integer,String>();
    static {
        mTokenTypes.put(Integer.valueOf(START_ELEMENT), "START_ELEMENT");
        mTokenTypes.put(Integer.valueOf(END_ELEMENT), "END_ELEMENT");
        mTokenTypes.put(Integer.valueOf(START_DOCUMENT), "START_DOCUMENT");
        mTokenTypes.put(Integer.valueOf(END_DOCUMENT), "END_DOCUMENT");
        mTokenTypes.put(Integer.valueOf(CHARACTERS), "CHARACTERS");
        mTokenTypes.put(Integer.valueOf(CDATA), "CDATA");
        mTokenTypes.put(Integer.valueOf(COMMENT), "COMMENT");
        mTokenTypes.put(Integer.valueOf(PROCESSING_INSTRUCTION), "PROCESSING_INSTRUCTION");
        mTokenTypes.put(Integer.valueOf(DTD), "DTD");
        mTokenTypes.put(Integer.valueOf(SPACE), "SPACE");
        mTokenTypes.put(Integer.valueOf(ENTITY_REFERENCE), "ENTITY_REFERENCE");
    }
    
    protected final XmlMapper XML_MAPPER = newMapper();
    
    protected XMLValidationSchema parseSchema(String contents, String schemaType) throws Exception
    {
        XMLValidationSchemaFactory schF = XMLValidationSchemaFactory.newInstance(schemaType);
        return schF.createSchema(new StringReader(contents));
    }

    protected XMLValidationSchema parseRngSchema(String contents) throws Exception
    {
        return parseSchema(contents, XMLValidationSchema.SCHEMA_ID_RELAXNG);
    }

    protected XMLValidationSchema parseDTDSchema(String contents) throws Exception
    {
        return parseSchema(contents, XMLValidationSchema.SCHEMA_ID_DTD);
    }

    protected XMLValidationSchema parseW3CSchema(String contents) throws Exception
    {
        return parseSchema(contents, XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA);
    }

    protected XMLStreamReader2 getXMLReader(String xml) throws XMLStreamException
    {
        return (XMLStreamReader2) XML_MAPPER.getFactory()
                .getXMLInputFactory()
                .createXMLStreamReader(new StringReader(xml));
    }

    protected void verifyFailure(String xml, XMLValidationSchema schema, String failMsg,
                                 String failPhrase) throws Exception
    {
        XMLStreamReader2 sr = getXMLReader(xml);
        sr.validateAgainst(schema);
        try {
            while (sr.hasNext()) {
                /* int type = */sr.next();
            }
            fail("Expected validity exception for " + failMsg);
        } catch (XMLValidationException vex) {
            String origMsg = vex.getMessage();
            String msg = (origMsg == null) ? "" : origMsg.toLowerCase();
            if (msg.indexOf(failPhrase.toLowerCase()) < 0) {
                String actualMsg = "Expected validation exception for "
                    + failMsg + ", containing phrase '" + failPhrase
                    + "': got '" + origMsg + "'";
                fail(actualMsg);
            }
            // should get this specific type; not basic stream exception
        } catch (XMLStreamException sex) {
            fail("Expected XMLValidationException for " + failMsg
                 + "; instead got " + sex.getMessage());
        }
    }    

    protected void assertTokenType(int expType, int actType)
    {
        if (expType != actType) {
            String expStr = tokenTypeDesc(expType);
            String actStr = tokenTypeDesc(actType);

            if (expStr == null) {
                expStr = ""+expType;
            }
            if (actStr == null) {
                actStr = ""+actType;
            }
            fail("Expected token "+expStr+"; got "+actStr+".");
        }
    }

    protected static String tokenTypeDesc(int tt)
    {
        String desc = mTokenTypes.get(Integer.valueOf(tt));
        return (desc == null) ? ("["+tt+"]") : desc;
    }
}
