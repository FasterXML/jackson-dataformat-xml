package tools.jackson.dataformat.xml.misc;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// For [dataformat-xml#531]
public class XmlNameEscapeTest extends XmlTestUtil
{
    // XML 1.0 `Name` production, limited to characters the base64 processors can
    // produce: only letters, `_` and `:` may START a name, digits and `-` may not.
    private final static Pattern VALID_XML_NAME = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:.-]*");

    public static class DTO {
        public Map<String, String> badMap = new HashMap<>();

        @Override
        public String toString() {
            return "DTO{" +
                    "badMap=" + badMap.entrySet().stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.joining(", ", "[", "]")) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DTO dto = (DTO) o;
            return Objects.equals(badMap, dto.badMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(badMap);
        }
    }

    @Test
    public void testGoodMapKeys() throws Exception {
        DTO dto = new DTO();

        dto.badMap.put("foo", "bar");
        dto.badMap.put("abc", "xyz");

        XmlMapper mapper = new XmlMapper();

        final String res = mapper.writeValueAsString(dto);

        DTO reversed = mapper.readValue(res, DTO.class);

        assertEquals(dto, reversed);
    }

    @Test
    public void testBase64() throws Exception {
        DTO dto = new DTO();

        dto.badMap.put("123", "bar");
        dto.badMap.put("$ I am <fancy>! &;", "xyz");
        dto.badMap.put("<!-- No comment=\"but' fancy tag!\"$ />", "xyz");

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        DTO reversed = mapper.readValue(res, DTO.class);
        assertEquals(dto, reversed);
    }

    @Test
    public void testAlwaysOnBase64() throws Exception {
        DTO dto = new DTO();

        dto.badMap.put("123", "bar");
        dto.badMap.put("$ I am <fancy>! &;", "xyz");
        dto.badMap.put("<!-- No comment=\"but' fancy tag!\"$ />", "xyz");

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newAlwaysOnBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        DTO reversed = mapper.readValue(res, DTO.class);
        assertEquals(dto, reversed);
    }

    // base64url's alphabet includes digits, but a digit can not start an XML name.
    // Names whose first character is U+0400 or above encode to a leading digit, so
    // the "always on" processor has to keep the encoded name a valid NameStartChar
    // and still round-trip.
    @Test
    public void testAlwaysOnBase64NonAsciiKeysRoundTrip() throws Exception {
        DTO dto = new DTO();
        // U+4E2D U+6587 (Chinese) encodes to a name starting with a digit
        dto.badMap.put(new String(new int[] { 0x4E2D, 0x6587 }, 0, 2), "cjk");
        // U+043F U+0440 U+0438 U+0432 (Cyrillic)
        dto.badMap.put(new String(new int[] { 0x43F, 0x440, 0x438, 0x432 }, 0, 4), "cyrillic");
        dto.badMap.put("abc", "ascii"); // starts with a letter, unchanged

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newAlwaysOnBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        // digit-leading encodings carry the `_` marker...
        assertTrue(res.contains("<_5Lit5paH>cjk</_5Lit5paH>"), res);
        assertTrue(res.contains("<_0L_RgNC40LI>cyrillic</_0L_RgNC40LI>"), res);
        // ... and letter-leading ones are written exactly as before
        assertTrue(res.contains("<YWJj>ascii</YWJj>"), res);

        DTO reversed = mapper.readValue(res, DTO.class);
        assertEquals(dto, reversed);
    }

    // U+0400 is the first code point whose base64url encoding begins with a digit
    // (lead byte 0xD0 -> index 52 -> '0'), so it is the exact boundary at which the
    // `_` marker starts being needed; U+03FF just below it still encodes to a letter.
    @Test
    public void testAlwaysOnBase64NameStartBoundary() throws Exception {
        DTO dto = new DTO();
        dto.badMap.put(new String(new int[] { 0x3FF }, 0, 1), "below");
        dto.badMap.put(new String(new int[] { 0x400 }, 0, 1), "at");

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newAlwaysOnBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        // U+03FF -> "z78", already a valid name start: left alone
        assertTrue(res.contains("<z78>below</z78>"), res);
        // U+0400 -> "0IA", needs the marker
        assertTrue(res.contains("<_0IA>at</_0IA>"), res);

        DTO reversed = mapper.readValue(res, DTO.class);
        assertEquals(dto, reversed);
    }

    public static class AttrDTO {
        // U+0400 U+0066 U+0069 U+0072 U+0073 U+0074 ("first" prefixed with Cyrillic IE)
        @JacksonXmlProperty(localName = "\u0400first", isAttribute = true)
        public String attr;

        protected AttrDTO() { }
        public AttrDTO(String a) { attr = a; }
    }

    // Attribute names have the same NameStartChar rule as element names and go
    // through the same processor, so the marker has to apply there too.
    @Test
    public void testAlwaysOnBase64AttributeNameRoundTrip() throws Exception {
        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newAlwaysOnBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(new AttrDTO("x"));
        assertTrue(res.contains("_0IBmaXJzdA=\"x\""), res);

        AttrDTO reversed = mapper.readValue(res, AttrDTO.class);
        assertEquals("x", reversed.attr);
    }

    // Whatever the name, the "always on" processor has to hold two invariants:
    // what it emits is a valid XML name, and decoding gives back exactly what was
    // encoded. Checked directly on the processor since some of these names (the
    // empty one in particular) can not be produced through a Map key.
    @Test
    public void testAlwaysOnBase64NameInvariants() throws Exception {
        final String[] names = new String[] {
                "", // degenerate, but must not fail
                "abc", // encodes to a letter: no marker needed
                "123",
                "$ I am <fancy>! &;",
                new String(new int[] { 0x3FF }, 0, 1), // last code point encoding to a letter
                new String(new int[] { 0x400 }, 0, 1), // first code point encoding to a digit
                new String(new int[] { 0x43F, 0x440, 0x438, 0x432 }, 0, 4), // Cyrillic
                new String(new int[] { 0x4E2D, 0x6587 }, 0, 2), // CJK
                new String(new int[] { 0x1F600 }, 0, 1), // emoji, 4-byte UTF-8
        };
        final XmlNameProcessor proc = XmlNameProcessors.newAlwaysOnBase64Processor();

        for (String name : names) {
            XmlNameProcessor.XmlName xmlName = new XmlNameProcessor.XmlName();
            xmlName.localPart = name;

            proc.encodeName(xmlName);
            final String encoded = xmlName.localPart;
            if (name.isEmpty()) {
                assertEquals("", encoded, "Empty name should encode to empty name");
            } else {
                assertTrue(VALID_XML_NAME.matcher(encoded).matches(),
                        "Invalid XML name '"+encoded+"' encoded from '"+name+"'");
            }

            proc.decodeName(xmlName);
            assertEquals(name, xmlName.localPart,
                    "Failed round-trip of '"+name+"' (encoded as '"+encoded+"')");
        }
    }

    @Test
    public void testReplace() throws Exception {
        DTO dto = new DTO();

        dto.badMap.put("123", "bar");
        dto.badMap.put("$ I am <fancy>! &;", "xyz");
        dto.badMap.put("<!-- No comment=\"but' fancy tag!\"$ />", "xyz");

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newReplacementProcessor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        DTO reversed = mapper.readValue(res, DTO.class);
        assertNotNull(reversed);
    }

    public static class BadVarNameDTO {
        public int $someVar$ = 5;
    }

    @Test
    public void testBadVarName() throws Exception {
        BadVarNameDTO dto = new BadVarNameDTO();

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        BadVarNameDTO reversed = mapper.readValue(res, BadVarNameDTO.class);
        assertEquals(dto.$someVar$, reversed.$someVar$);
    }

    // A base64 name processor decodes incoming element/attribute names by
    // running java.util.Base64 over them. For a name that is not valid base64
    // that decoder throws IllegalArgumentException; reading untrusted XML should
    // surface it as a StreamReadException, not let a raw runtime exception escape.
    @Test
    public void testBase64UndecodableNameFailsAsReadException() throws Exception {
        // prefix present but remainder ("a") is not valid base64
        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newBase64Processor())
        ).build();
        StreamReadException e = assertThrows(StreamReadException.class, () ->
                mapper.readValue("<root><base64_tag_a>x</base64_tag_a></root>", DTO.class));
        // and it should be surfaced as a located read error, not with the
        // unknown/NA sentinel (regression guard for the location being attached)
        assertNotEquals(TokenStreamLocation.NA, e.getLocation());
    }

    @Test
    public void testAlwaysOnBase64UndecodableNameFailsAsReadException() throws Exception {
        // "always on" decodes every name; a single-character root name is not
        // valid base64, and this must fail while still parsing the stream
        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newAlwaysOnBase64Processor())
        ).build();
        StreamReadException e = assertThrows(StreamReadException.class, () ->
                mapper.readValue("<a>x</a>", DTO.class));
        assertNotEquals(TokenStreamLocation.NA, e.getLocation());
    }

    // A name that is a valid XML name but already starts with the magic prefix has
    // to be escaped too: decoding keys off that prefix, so writing such a name
    // through as-is makes it read back as a different name than was written.
    @Test
    public void testBase64PrefixedNamesRoundTrip() throws Exception {
        DTO dto = new DTO();

        // would otherwise read back as "admin"
        dto.badMap.put("base64_tag_YWRtaW4", "xyz");
        // valid XML name, but not valid base64 past the prefix
        dto.badMap.put("base64_tag_hello", "bar");
        dto.badMap.put("plain", "abc");

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newBase64Processor())
        ).build();

        final String res = mapper.writeValueAsString(dto);
        // Prefixed names get escaped...
        assertTrue(res.contains("base64_tag_YmFzZTY0X3RhZ19ZV1J0YVc0"), res);
        assertTrue(res.contains("base64_tag_YmFzZTY0X3RhZ19oZWxsbw"), res);
        // ... but ordinary valid names still pass through as-is
        assertTrue(res.contains("<plain>abc</plain>"), res);

        DTO reversed = mapper.readValue(res, DTO.class);
        assertEquals(dto, reversed);
    }

    @Test
    public void testBase64CustomPrefixedNameRoundTrip() throws Exception {
        DTO dto = new DTO();
        dto.badMap.put("esc_YWRtaW4", "xyz");

        XmlMapper mapper = XmlMapper.builder(
                xmlFactory(XmlNameProcessors.newBase64Processor("esc_"))
        ).build();

        final String res = mapper.writeValueAsString(dto);
        assertTrue(res.contains("esc_ZXNjX1lXUnRhVzQ"), res);

        DTO reversed = mapper.readValue(res, DTO.class);
        assertEquals(dto, reversed);
    }

    // Null prefix/replacement would otherwise only blow up deep inside name handling
    @Test
    public void testBase64NullPrefixFailsAtConstruction() throws Exception {
        assertThrows(NullPointerException.class,
                () -> XmlNameProcessors.newBase64Processor(null));
    }

    @Test
    public void testReplacementNullFailsAtConstruction() throws Exception {
        assertThrows(NullPointerException.class,
                () -> XmlNameProcessors.newReplacementProcessor(null));
    }

    protected XmlFactory xmlFactory(XmlNameProcessor proc) {
        return XmlFactory.builder().xmlNameProcessor(proc).build();
    }
}
