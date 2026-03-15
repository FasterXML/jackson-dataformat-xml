package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#423]
public class XmlTextViaCtor423Test extends XmlTestUtil
{
    static class Sample423
    {
        final String text;
        final String attribute;

        @JsonCreator
        public Sample423(@JacksonXmlText String text,
                @JacksonXmlProperty(localName = "attribute", isAttribute = true)
                String attribute) {
            this.text = text;
            this.attribute = attribute;
        }
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#423]
    @Test
    public void testXmlTextViaCtor423() throws Exception
    {
        final String XML = "<Sample423 attribute='attrValue'>text value</Sample423>";
        Sample423 result = MAPPER.readValue(XML, Sample423.class);
        assertEquals("attrValue", result.attribute);
        assertEquals("text value", result.text);
    }
}
