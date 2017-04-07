package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class MixedContentTreeRead226Test extends XmlTestBase
{
    public void testMixed226() throws Exception
    {
        final String XML = "<root>\n<a>lorem <b>ipsum</b> dolor</a>\n</root>";
        XmlMapper mapper = new XmlMapper();
        JsonNode root = mapper.readTree(XML);
        assertNotNull(root);
    }
}
