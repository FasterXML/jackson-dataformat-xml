package com.fasterxml.jackson.dataformat.xml.stream.dos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TokenCountTest extends XmlTestBase {

    public void testTokenCount10() throws Exception
    {
        final XmlFactory factory = new XmlFactory();
        // token count is only checked when maxTokenCount is set
        factory.setStreamReadConstraints(StreamReadConstraints.builder()
            .maxTokenCount(1000)
            .build());
        final XmlMapper xmlMapper = mapperBuilder(factory).build();
        final String XML = createDeepNestedDoc(10);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
            assertEquals(31, p.currentTokenCount());
        }
    }

    public void testTokenCount100() throws Exception
    {
        final XmlFactory factory = new XmlFactory();
        // token count is only checked when maxTokenCount is set
        factory.setStreamReadConstraints(StreamReadConstraints.builder()
            .maxTokenCount(1000)
            .build());
        final XmlMapper xmlMapper = mapperBuilder(factory).build();
        final String XML = createDeepNestedDoc(100);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
            assertEquals(301, p.currentTokenCount());
        }
    }

    public void testDeepDoc() throws Exception
    {
        final XmlFactory factory = new XmlFactory();
        factory.setStreamReadConstraints(StreamReadConstraints.builder()
            .maxTokenCount(1000)
            .build());
        final XmlMapper xmlMapper = mapperBuilder(factory).build();
        final String XML = createDeepNestedDoc(1000);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
            fail("expected StreamReadException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("Token count (1001) exceeds the maximum allowed"));
        }
    }

    private String createDeepNestedDoc(final int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a>");
        for (int i = 0; i < depth; i++) {
            sb.append("<a>");
        }
        sb.append("a");
        for (int i = 0; i < depth; i++) {
            sb.append("</a>");
        }
        sb.append("</a>");
        return sb.toString();
    }
}
