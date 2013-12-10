package com.fasterxml.jackson.dataformat.xml.unwrapped;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class TestXmlText extends XmlTestBase{
    @JsonPropertyOrder({"first","second"})
    class Data{
        @JacksonXmlText
        public String first;
        public String second;
        public Data(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
    
    private final XmlMapper MAPPER = new XmlMapper();
    { // easier for eye, uncomment for testing
//        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public void testXmlTextWithSuppressedValue() throws Exception {
        MAPPER.setSerializationInclusion(Include.NON_EMPTY);
        String xml = MAPPER.writeValueAsString(new Data("","second"));
        String expectedXml = "<Data><second>second</second></Data>";
        assertEquals(expectedXml, xml);
    }
}
