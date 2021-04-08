package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class XsiNilEnableDisable467Test extends XmlTestBase
{
    private final static String XSI_NS_DECL = "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'";

    private final XmlMapper MAPPER = newMapper();

    // [databind#467] / [databind#467]: xsi:nil handling of nested elements etc
    public void testNilAsNodeLeaf() throws Exception
    {
        JsonNode node = MAPPER.readTree(
"<e>"
+"<h xmlns:xsi='"+XSI_NS_DECL+"' xsi:nil='true'><child /></h>"
+"</e>"
                );
        assertNotNull(node);
    }
}
