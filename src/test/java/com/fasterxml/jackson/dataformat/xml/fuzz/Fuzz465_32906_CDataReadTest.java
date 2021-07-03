package com.fasterxml.jackson.dataformat.xml.fuzz;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class Fuzz465_32906_CDataReadTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testIssue465() throws Exception
    {
        byte[] doc = readResource("/data/fuzz-32906.xml");
        try {
            JsonNode root = MAPPER.readTree(doc);
            fail("Should not pass, got: "+root);
        } catch (StreamReadException e) {
            verifyException(e, "Unexpected EOF in CDATA");
        }
    }
}
