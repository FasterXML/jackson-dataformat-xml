package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#269], [dataformat-xml#448]
public class RawValueSerializationTest extends XmlTestUtil
{
    // [dataformat-xml#269]
    @JsonPropertyOrder({ "id", "raw" })
    public static class RawWrapper {
        public int id = 42;

        @JsonRawValue
        public String raw = "Mixed <b>content</b>";
    }

    // [dataformat-xml#448]
    @JsonPropertyOrder({ "id", "raw" })
    public static class RawWrapperWithXmlText {
        public int id = 42;

        @JacksonXmlText
        @JsonRawValue
        public String raw = "Mixed <b>content</b>";
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    @Test
    void testRawValueSerializationRegular() throws Exception
    {
        assertEquals("<RawWrapper>"
                +"<id>42</id>"
                +"<raw>Mixed <b>content</b></raw>"
                +"</RawWrapper>",
                MAPPER.writeValueAsString(new RawWrapper()).trim());
    }

    @Test
    void testRawValueSerializationWithoutWrapping() throws Exception
    {
        assertEquals("<RawWrapperWithXmlText>"
                +"<id>42</id>"
                +"Mixed <b>content</b>"
                +"</RawWrapperWithXmlText>",
                MAPPER.writeValueAsString(new RawWrapperWithXmlText()).trim());
    }
}
