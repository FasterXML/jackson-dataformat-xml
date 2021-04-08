package com.fasterxml.jackson.dataformat.xml.fuzz;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// [dataformat-xml#???]
// (but root cause of https://github.com/FasterXML/woodstox/issues/125)
//
// NOTE! Not reproducible for some reason with these settings (probably
// has different buffer sizes or... something
public class FuzzXXX_32969_UTF32Test extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testUTF32() throws Exception
    {
        final byte[] doc = readResource("/data/fuzz-32906.xml");
        try {
            MAPPER.readTree(doc, 0, doc.length);
            fail("Should not pass");
        } catch (StreamReadException e) {
            verifyException(e, "Unexpected EOF in CDATA");
        } catch (RuntimeException e) {
            fail("Should fail with specific `StreamReadException` but got: "+e);
        }
    }
}
