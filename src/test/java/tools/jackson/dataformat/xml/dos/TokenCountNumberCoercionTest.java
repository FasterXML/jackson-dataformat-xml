package tools.jackson.dataformat.xml.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.StreamReadConstraints;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml] `isExpectedNumberIntToken()` must count the coercion to
// VALUE_NUMBER_INT the same way regardless of the integer's digit length, so
// that `currentTokenCount()` and the `maxTokenCount` read constraint stay
// consistent.
public class TokenCountNumberCoercionTest extends XmlTestUtil
{
    private final XmlMapper MAPPER;
    {
        final XmlFactory f = XmlFactory.builder()
                // token count is only tracked when maxTokenCount is set
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxTokenCount(1_000)
                        .build())
                .build();
        MAPPER = mapperBuilder(f).build();
    }

    // Drive parser to the scalar, coerce it via isExpectedNumberIntToken() and
    // return the token count observed right after the coercion.
    private long countAfterIntCoercion(String number) throws Exception
    {
        final String xml = "<r><v>" + number + "</v></r>";
        try (JsonParser p = MAPPER.createParser(xml)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertTrue(p.isExpectedNumberIntToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.currentToken());
            return p.currentTokenCount();
        }
    }

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
}
