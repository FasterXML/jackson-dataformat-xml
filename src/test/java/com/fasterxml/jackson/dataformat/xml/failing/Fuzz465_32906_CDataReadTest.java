package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class Fuzz465_32906_CDataReadTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testIssue465() throws Exception
    {
        byte[] doc = readResource("/data/fuzz-32906.xml");
        JsonNode root = MAPPER.readTree(doc);
        assertNotNull(root);
    }
}
