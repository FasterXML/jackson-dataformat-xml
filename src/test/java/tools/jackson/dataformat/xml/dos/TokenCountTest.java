package tools.jackson.dataformat.xml.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.exc.StreamConstraintsException;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class TokenCountTest extends XmlTestUtil
{
    final XmlMapper XML_MAPPER;
    {
        final XmlFactory factory = XmlFactory.builder()
                // token count is only checked when maxTokenCount is set
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxTokenCount(1000)
                        .build())
                .build();
        XML_MAPPER = mapperBuilder(factory).build();
    }

    @Test
    public void testTokenCount10() throws Exception
    {
        final String XML = createDeepNestedDoc(10);
        try (JsonParser p = XML_MAPPER.createParser(XML)) {
            while (p.nextToken() != null) { }
            assertEquals(31, p.currentTokenCount());
        }
    }

    @Test
    public void testTokenCount100() throws Exception
    {
        final String XML = createDeepNestedDoc(100);
        try (JsonParser p = XML_MAPPER.createParser(XML)) {
            while (p.nextToken() != null) { }
            assertEquals(301, p.currentTokenCount());
        }
    }

    @Test
    public void testDeepDoc() throws Exception
    {
        final String XML = createDeepNestedDoc(1000);
        try (JsonParser p = XML_MAPPER.createParser(XML)) {
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
