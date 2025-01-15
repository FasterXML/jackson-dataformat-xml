package com.fasterxml.jackson.dataformat.xml.fuzz;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;

import com.fasterxml.jackson.dataformat.xml.*;

public class Fuzz618_64655_InvalidXMLTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testWithInvalidXml1() throws Exception {
        _testWithInvalidXml(1, "Unexpected end of input", // Woodstox
                "Internal processing error by `XMLStreamReader` of type" // SJSXP
        );
    }

    @Test
    public void testWithInvalidXml2() throws Exception {
        _testWithInvalidXml(2, "Unexpected character 'a'", // Woodstox
                "Internal processing error by `XMLInputFactory` of type " // SJSXP
        );
    }

    @Test
    public void testWithInvalidXml3() throws Exception {
        _testWithInvalidXml(3, "Unexpected EOF; was expecting a close tag", // Woodstox
                "XML document structures must start and end" // SJSXP
        );
    }

    private void _testWithInvalidXml(int ix, String... errorToMatch) throws Exception
    {
        byte[] doc = readResource("/data/fuzz-618-"+ix+".xml");
        try {
            MAPPER.readTree(doc);
        } catch (StreamReadException e) {
            verifyException(e, errorToMatch);
        }
    }
}
