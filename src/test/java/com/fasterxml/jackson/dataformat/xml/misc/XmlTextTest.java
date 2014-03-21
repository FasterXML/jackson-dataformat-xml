package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class XmlTextTest extends XmlTestBase
{
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

    public void testXmlTextWithSuppressedValue() throws Exception
    {
        final XmlMapper mapper = new XmlMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String xml = mapper.writeValueAsString(new Data("","second"));
        String expectedXml = "<Data><second>second</second></Data>";
        assertEquals(expectedXml, xml);
    }
}
