package com.fasterxml.jackson.dataformat.xml.fuzz;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#463]
// (but root cause of https://github.com/FasterXML/woodstox/issues/123)
public class Fuzz463_32872_XmlDeclTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
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
