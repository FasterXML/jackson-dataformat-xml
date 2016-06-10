package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class XmlTextTest extends XmlTestBase
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

    public void testXmlTextWithSuppressedValue() throws Exception
    {
        final XmlMapper mapper = new XmlMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String xml = mapper.writeValueAsString(new Data("","second"));
        String expectedXml = "<Data><second>second</second></Data>";
        assertEquals(expectedXml, xml);
    }

    // for [dataformat-xml#198]
    public void testSimple198() throws Exception
    {
        final XmlMapper mapper = new XmlMapper();
        String xml = mapper.writeValueAsString(new Phone());
        Phone result = mapper.readValue(xml, Phone.class);
        assertNotNull(result);
    }
}
