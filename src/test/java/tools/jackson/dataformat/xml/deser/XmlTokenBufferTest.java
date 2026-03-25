package tools.jackson.dataformat.xml.deser;

import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;

import tools.jackson.databind.util.TokenBuffer;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link XmlTokenBuffer.ElementWrappableParser} virtual wrapping
 * state machine, verifying that repeated property names are transformed into
 * JSON arrays in the buffered token stream.
 */
public class XmlTokenBufferTest extends XmlTestUtil
{
    // No-op ElementWrappable for tests that only exercise local wrapping
    static final ElementWrappable NOOP_WRAPPABLE = (names, caseInsensitive) -> { };

    /*
    /**********************************************************************
    /* Basic wrapping tests
    /**********************************************************************
     */

    // Repeated string values: {item:"a", item:"b"} → {item:["a","b"]}
    @Test
    public void testWrappingRepeatedStrings() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("item");
            b.writeString("a");
            b.writeName("item");
            b.writeString("b");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("item"), false)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("item", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertTrue(p.isExpectedStartArrayToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("a", p.getString());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("b", p.getString());
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    // Repeated objects: {item:{x:1}, item:{x:2}} → {item:[{x:1},{x:2}]}
    @Test
    public void testWrappingRepeatedObjects() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("item");
            b.writeStartObject();
            b.writeName("x");
            b.writeNumber(1);
            b.writeEndObject();
            b.writeName("item");
            b.writeStartObject();
            b.writeName("x");
            b.writeNumber(2);
            b.writeEndObject();
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("item"), false)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("item", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            // first object
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("x", p.currentName());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            // second object
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("x", p.currentName());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            // end of virtual array and object
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    /*
    /**********************************************************************
    /* Single-element and mixed property tests
    /**********************************************************************
     */

    // Single wrapped element followed by another property:
    // {item:"a", other:"b"} → {item:["a"], other:"b"}
    @Test
    public void testWrappingSingleElement() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("item");
            b.writeString("a");
            b.writeName("other");
            b.writeString("b");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("item"), false)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("item", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("a", p.getString());
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            // non-wrapped property follows
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("other", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("b", p.getString());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    // Wrapped property at end of object (no trailing properties):
    // {other:"x", item:"a", item:"b"} → {other:"x", item:["a","b"]}
    @Test
    public void testWrappedPropertyAtEnd() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("other");
            b.writeString("x");
            b.writeName("item");
            b.writeString("a");
            b.writeName("item");
            b.writeString("b");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("item"), false)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("other", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("x", p.getString());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("item", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("a", p.getString());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("b", p.getString());
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    /*
    /**********************************************************************
    /* Multiple wrapped properties
    /**********************************************************************
     */

    // Two different wrapped properties:
    // {a:"1", a:"2", b:"3", b:"4"} → {a:["1","2"], b:["3","4"]}
    @Test
    public void testTwoDifferentWrappedProperties() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("a");
            b.writeString("1");
            b.writeName("a");
            b.writeString("2");
            b.writeName("b");
            b.writeString("3");
            b.writeName("b");
            b.writeString("4");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("a", "b"), false)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            // first wrapped: a
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("a", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("1", p.getString());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("2", p.getString());
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            // second wrapped: b
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("b", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("3", p.getString());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("4", p.getString());
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    /*
    /**********************************************************************
    /* Case-insensitive wrapping
    /**********************************************************************
     */

    @Test
    public void testCaseInsensitiveWrapping() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("Item");
            b.writeString("a");
            b.writeName("ITEM");
            b.writeString("b");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("item"), true)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("Item", p.currentName());
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("a", p.getString());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("b", p.getString());
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    /*
    /**********************************************************************
    /* No wrapping when not configured
    /**********************************************************************
     */

    // Non-wrapped names should pass through unchanged
    @Test
    public void testNonWrappedNamesPassThrough() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("x");
            b.writeString("1");
            b.writeName("y");
            b.writeString("2");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("other"), false)) {
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("x", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("1", p.getString());
            assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
            assertEquals("y", p.currentName());
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("2", p.getString());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    // Before addVirtualWrapping is called, tokens pass through unchanged
    @Test
    public void testNoWrappingBeforeConfiguration() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("item");
            b.writeString("a");
            b.writeName("item");
            b.writeString("b");
            b.writeEndObject();
        });
        // Create parser WITHOUT calling addVirtualWrapping
        XmlTokenBuffer.ElementWrappableParser p =
                new XmlTokenBuffer.ElementWrappableParser(buf.asParser(), NOOP_WRAPPABLE);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
        assertEquals("item", p.currentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a", p.getString());
        assertToken(JsonToken.PROPERTY_NAME, p.nextToken());
        assertEquals("item", p.currentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("b", p.getString());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    /*
    /**********************************************************************
    /* Token query consistency tests
    /**********************************************************************
     */

    @Test
    public void testTokenQueryMethodsOnVirtualTokens() throws Exception
    {
        TokenBuffer buf = _bufferWith(b -> {
            b.writeStartObject();
            b.writeName("item");
            b.writeString("a");
            b.writeEndObject();
        });
        try (JsonParser p = _wrappedParser(buf, Set.of("item"), false)) {
            p.nextToken(); // START_OBJECT

            p.nextToken(); // PROPERTY_NAME "item"
            assertTrue(p.hasToken(JsonToken.PROPERTY_NAME));
            assertFalse(p.isExpectedStartArrayToken());

            p.nextToken(); // virtual START_ARRAY
            assertTrue(p.isExpectedStartArrayToken());
            assertFalse(p.isExpectedStartObjectToken());
            assertTrue(p.hasToken(JsonToken.START_ARRAY));
            assertTrue(p.hasTokenId(JsonToken.START_ARRAY.id()));
            assertTrue(p.hasCurrentToken());

            p.nextToken(); // VALUE_STRING "a"
            p.nextToken(); // virtual END_ARRAY
            assertTrue(p.hasToken(JsonToken.END_ARRAY));
            assertFalse(p.isExpectedStartArrayToken());

            p.nextToken(); // END_OBJECT (pending)
            assertTrue(p.hasToken(JsonToken.END_OBJECT));
        }
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    @FunctionalInterface
    interface BufferWriter {
        void write(TokenBuffer buf) throws Exception;
    }

    private TokenBuffer _bufferWith(BufferWriter writer) throws Exception
    {
        TokenBuffer buf = TokenBuffer.forGeneration();
        writer.write(buf);
        return buf;
    }

    private JsonParser _wrappedParser(TokenBuffer buf,
            Set<String> namesToWrap, boolean caseInsensitive)
    {
        JsonParser raw = buf.asParser();
        XmlTokenBuffer.ElementWrappableParser p =
                new XmlTokenBuffer.ElementWrappableParser(raw, NOOP_WRAPPABLE);
        p.addVirtualWrapping(namesToWrap, caseInsensitive);
        return p;
    }
}
