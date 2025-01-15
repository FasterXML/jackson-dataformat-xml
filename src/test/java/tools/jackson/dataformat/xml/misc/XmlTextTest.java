package tools.jackson.dataformat.xml.misc;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XmlTextTest extends XmlTestUtil
{
    @JsonPropertyOrder({"first","second"})
    static class Data{
        @JacksonXmlText
        public String first;
        public String second;
        public Data(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    static class Phone
    {
        @JacksonXmlProperty(isAttribute = true)
        public String phoneType = "mobile";

        @JacksonXmlText
        public String phoneNumber = "555-1234";
    }

    static class WindSpeed {
        @JacksonXmlProperty(isAttribute = true)
        public String units;

        @JacksonXmlText
        public int value;

        public Radius radius;
    }

    static class Radius {
        @JacksonXmlText
        public int value;
    }

    // for [dataformat-xml#545]
    static class RawValue545 {
        @JacksonXmlText
        @JsonRawValue
        public String foo = "<a>b</a>";
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = xmlMapper(true);
    
    @Test
    public void testXmlTextWithSuppressedValue() throws Exception
    {
        final XmlMapper mapper = XmlMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_EMPTY))
                .build();
        String xml = mapper.writeValueAsString(new Data("","second"));
        String expectedXml = "<Data><second>second</second></Data>";
        assertEquals(expectedXml, xml);
    }

    // for [dataformat-xml#196]
    @Test
    public void testMixedContent() throws Exception
    {
        WindSpeed result = MAPPER.readValue("<windSpeed units='kt'> 27 <radius>20</radius></windSpeed>",
                WindSpeed.class);
        assertEquals(27, result.value);
        assertNotNull(result.radius);
        assertEquals(20, result.radius.value);
    }

    // for [dataformat-xml#198]
    @Test
    public void testSimple198() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Phone());
        Phone result = MAPPER.readValue(xml, Phone.class);
        assertNotNull(result);
    }

    // for [dataformat-xml#545]
    @Test
    public void testRawValue545() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new RawValue545());
        assertEquals("<RawValue545><a>b</a></RawValue545>", xml);
    }
}
