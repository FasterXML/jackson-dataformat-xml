package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class XmlTextWithEmpty449Test extends XmlTestBase
{
    static class Project449 {
        public Text449 text;
    }

    static class Project449WithList {
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Text449> text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Text449 {
        @JacksonXmlText
        public String content;
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#449]
    public void testXmlText449ItemWithAttr() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(Project449.class);
        Project449 p;

        // first, successful case:
        p = r.readValue("<project><text> </text></project>");
        assertNotNull(p.text);
        assertEquals(" ", p.text.content);

        // then fail if attribute
        p = r.readValue("<project><text id='test'> </text></project>");
        assertNotNull(p.text);
        assertEquals(" ", p.text.content);
    }

    // [dataformat-xml#449]
    public void testXmlText449ItemWithList() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(Project449WithList.class);
        Project449WithList p = r.readValue(
"<project><text> </text><text>hello world!</text></project>"
                );
        assertEquals(2, p.text.size());
        assertEquals("hello world!", p.text.get(1).content);
        assertEquals(" ", p.text.get(0).content);
    }
}
