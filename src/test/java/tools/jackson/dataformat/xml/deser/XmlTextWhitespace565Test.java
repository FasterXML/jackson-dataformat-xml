package tools.jackson.dataformat.xml.deser;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#565] Whitespace-only text content ignored with @JacksonXmlText
public class XmlTextWhitespace565Test extends XmlTestUtil
{
    @JacksonXmlRootElement(localName = "replacements")
    static class ClangFormatResponse {
        @JacksonXmlProperty(localName = "replacement")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Replacement> replacements = new ArrayList<>();
    }

    static class Replacement {
        @JacksonXmlProperty(isAttribute = true)
        public int offset;

        @JacksonXmlProperty(isAttribute = true)
        public int length;

        @JacksonXmlText
        public String value;
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testWhitespaceTextWithAttributes565() throws Exception
    {
        String xml = "<replacements>"
                + "<replacement offset='106' length='8'>    </replacement>"
                + "</replacements>";
        ClangFormatResponse resp = MAPPER.readValue(xml, ClangFormatResponse.class);
        assertNotNull(resp.replacements);
        assertEquals(1, resp.replacements.size());
        assertEquals(106, resp.replacements.get(0).offset);
        assertEquals(8, resp.replacements.get(0).length);
        assertEquals("    ", resp.replacements.get(0).value);
    }

    @Test
    public void testMultipleReplacementsWithWhitespace565() throws Exception
    {
        String xml = "<replacements>"
                + "<replacement offset='10' length='2'>  </replacement>"
                + "<replacement offset='20' length='1'> </replacement>"
                + "</replacements>";
        ClangFormatResponse resp = MAPPER.readValue(xml, ClangFormatResponse.class);
        assertEquals(2, resp.replacements.size());
        assertEquals("  ", resp.replacements.get(0).value);
        assertEquals(" ", resp.replacements.get(1).value);
    }

    @Test
    public void testNonWhitespaceTextWithAttributes565() throws Exception
    {
        String xml = "<replacements>"
                + "<replacement offset='10' length='3'>abc</replacement>"
                + "</replacements>";
        ClangFormatResponse resp = MAPPER.readValue(xml, ClangFormatResponse.class);
        assertEquals(1, resp.replacements.size());
        assertEquals("abc", resp.replacements.get(0).value);
    }
}
