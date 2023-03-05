package com.fasterxml.jackson.dataformat.xml.stream.dos;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class DeepNestingParserTest extends XmlTestBase {

    public void testDeepDoc() throws Exception
    {
        final XmlMapper xmlMapper = newMapper();
        final String XML = createDeepNestedDoc(1050);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            JsonToken jt;
            while ((jt = p.nextToken()) != null) {

            }
            fail("expected JsonParseException");
        } catch (JsonParseException e) {
            assertEquals("Maximum Element Depth limit (1000) Exceeded", e.getMessage());
        }
    }

    public void testDeepDocWithCustomDepthLimit() throws Exception
    {
        final WstxInputFactory wstxInputFactory = new WstxInputFactory();
        wstxInputFactory.getConfig().setMaxElementDepth(2000);
        final XmlMapper xmlMapper = new XmlMapper(wstxInputFactory);
        final String XML = createDeepNestedDoc(1050);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            JsonToken jt;
            while ((jt = p.nextToken()) != null) {

            }
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