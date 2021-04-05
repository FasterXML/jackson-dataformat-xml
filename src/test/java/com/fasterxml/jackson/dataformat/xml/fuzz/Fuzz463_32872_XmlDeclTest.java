package com.fasterxml.jackson.dataformat.xml.fuzz;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// [dataformat-xml#463]
// (but root cause of https://github.com/FasterXML/woodstox/issues/123)
public class Fuzz463_32872_XmlDeclTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testInvalidXmlDecl() throws Exception
    {
        final byte[] doc = "<?xml version=\"1.1\" encoding=\"U\"?>".getBytes(StandardCharsets.UTF_8);
        try {
            MAPPER.readTree(doc);
            fail("Should not pass");
        } catch (StreamReadException e) {
            verifyException(e, "Unsupported encoding: U");
        } catch (RuntimeException e) {
            fail("Should fail with specific `StreamReadException` but got: "+e);
        }
    }
}
