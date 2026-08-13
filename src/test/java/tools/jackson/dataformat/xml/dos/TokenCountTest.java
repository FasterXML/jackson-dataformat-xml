package tools.jackson.dataformat.xml.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
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

    // [dataformat-xml] `isExpectedNumberIntToken()` must count the coercion to
    // VALUE_NUMBER_INT the same way regardless of the integer's digit length, so
    // that `currentTokenCount()` and the `maxTokenCount` read constraint stay
    // consistent.
    @Test
    public void testIntCoercionTokenCountByLength() throws Exception
    {
        // 18-digit value stays within `long` via the <= 18 branch...
        final long len18 = countAfterIntCoercion("123456789012345678");
        // ...and a 19-digit value still within `long` range takes a separate
        // branch that previously skipped the token-count update.
        final long len19 = countAfterIntCoercion("1234567890123456789");
        // BigInteger branch for completeness
        final long lenBig = countAfterIntCoercion("123456789012345678901234");

        assertEquals(len18, len19,
                "19-digit long value should be counted like the 18-digit case");
        assertEquals(len18, lenBig,
                "BigInteger value should be counted like the 18-digit case");
    }

    // Drive parser to the scalar, coerce it via isExpectedNumberIntToken() and
    // return the token count observed right after the coercion.
    private long countAfterIntCoercion(String number) throws Exception
    {
        final String xml = "<r><v>" + number + "</v></r>";
        try (JsonParser p = XML_MAPPER.createParser(xml)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertTrue(p.isExpectedNumberIntToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.currentToken());
            return p.currentTokenCount();
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
