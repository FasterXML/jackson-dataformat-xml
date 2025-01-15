package tools.jackson.dataformat.xml.misc;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    protected XmlFactory xmlFactory(XmlNameProcessor proc) {
        return XmlFactory.builder().xmlNameProcessor(proc).build();
    }
}
