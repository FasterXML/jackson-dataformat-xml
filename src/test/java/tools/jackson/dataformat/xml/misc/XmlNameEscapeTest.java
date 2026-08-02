package tools.jackson.dataformat.xml.misc;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// For [dataformat-xml#531]
public class XmlNameEscapeTest extends XmlTestUtil
{
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
