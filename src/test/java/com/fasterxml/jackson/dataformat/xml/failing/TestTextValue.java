package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class TestTextValue extends XmlTestBase
{
    static class TextOnlyBean
    {
        @JacksonXmlText
        protected String textValue;

        public TextOnlyBean() { }
        public TextOnlyBean(String str, boolean foo) { textValue = str; }
    }

    @JsonPropertyOrder({ "a", "b" })
    static class TextOnlyWrapper
    {
        public TextOnlyBean a, b;

        public TextOnlyWrapper() { }
        public TextOnlyWrapper(String a, String b) {
            this.a = new TextOnlyBean(a, true);
            this.b = new TextOnlyBean(b, true);
        }
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // [Issue#72]
    public void testTextOnlyPojo() throws Exception
    {
        XmlMapper mapper = xmlMapper(true);
        TextOnlyWrapper input = new TextOnlyWrapper("foo", "bar");
        // serialization should work fine
        String xml = mapper.writeValueAsString(input);
        assertEquals("<TextOnlyWrapper><a>foo</a><b>bar</b></TextOnlyWrapper>", xml);
        // but how about deser?
        TextOnlyWrapper result = mapper.readValue(xml, TextOnlyWrapper.class);
        assertNotNull(result);
        assertEquals("foo", result.a.textValue);
        assertEquals("bar", result.b.textValue);
    }
}
