package com.fasterxml.jackson.dataformat.xml.woodstox;

import com.ctc.wstx.stax.WstxInputFactory;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

public class DeepNestingWoodstoxParserTest extends XmlTestUtil
{
    // Try using Woodstox-specific settings above and beyond
    // what Jackson-core would provide
    @Test
    public void testDeepDocWithWoodstoxLimits() throws Exception
    {
        final WstxInputFactory wstxInputFactory = new WstxInputFactory();
        wstxInputFactory.getConfig().setMaxElementDepth(2000);
        final XmlMapper xmlMapper = new XmlMapper(wstxInputFactory);
        final String XML = createDeepNestedDoc(1050);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
        }
    }

    private String createDeepNestedDoc(final int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("<root>");
        for (int i = 0; i < depth; i++) {
            sb.append("<leaf>");
        }
        sb.append("abc");
        for (int i = 0; i < depth; i++) {
            sb.append("</leaf>");
        }
        sb.append("</root>");
        return sb.toString();
    }
}
