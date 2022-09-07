package com.fasterxml.jackson.dataformat.xml.misc;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class TextValueTest extends XmlTestBase
{
    static class Simple
    {
        @JacksonXmlProperty(isAttribute=true)
        // same as: @jakarta.xml.bind.annotation.XmlAttribute
        public int a = 13;

        @JacksonXmlText
        // about same as: @jakarta.xml.bind.annotation.XmlValue
        public String text = "something";
    }
	
    // [dataformat-xml#24]

    static class Main {
        @JsonProperty("com.test.stack") public Stack stack;
    }
    static class Stack {
        public String name;

        @JsonProperty("com.test.stack.slot")
        public Slot slot;
    }
    static class Slot {
        @JsonProperty("name")
        public String name;

        @JsonProperty("id")
        public String id;

        @JsonProperty("height")
        public String height;

        @JsonProperty("width")
        public String width;

        @JacksonXmlText
        public String value;
    }

    static class JAXBStyle
    {
        public String value;
    }

    // [dataformat-xml#66]
    static class Issue66Bean
    {
        @JacksonXmlProperty(isAttribute = true)
        protected String id;

        @JacksonXmlText
        protected String textValue;
    }

    // [dataformat-xml#72]
    
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

    private final XmlMapper MAPPER = new XmlMapper();
    
    public void testSerializeAsText() throws IOException
    {
        String xml = MAPPER.writeValueAsString(new Simple());
        assertEquals("<Simple a=\"13\">something</Simple>", xml);
        // [dataformat-xml#56]: should work with indentation as well
        xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new Simple());
        assertEquals("<Simple a=\"13\">something</Simple>\n", xml);
    }

    public void testDeserializeAsText() throws IOException
    {
        Simple result = MAPPER.readValue("<Simple a='99'>else</Simple>", Simple.class);
        assertEquals(99, result.a);
        assertEquals("else", result.text);
    }
    
    public void testIssue24() throws Exception
    {
        final String TEXT = "+/null/this is a long string";
        final String XML =
    			"<main>\n"
    			+"<com.test.stack name='stack1'>\n"
    			+"<com.test.stack.slot height='0' id='0' name='slot0' width='0'>"
    			+TEXT
    			+"</com.test.stack.slot>\n"
    			+"</com.test.stack>\n"
    			+"</main>";
        Main main = MAPPER.readValue(XML, Main.class);
        assertNotNull(main.stack);
        assertNotNull(main.stack.slot);
        assertEquals(TEXT, main.stack.slot.value);
    }

    // for [dataformat-xml#36]
    public void testAlternateTextElementName() throws IOException
    {
        final String XML = "<JAXBStyle>foo</JAXBStyle>";
        // first: verify that without change, POJO would not match:
        try {
            MAPPER.readValue(XML, JAXBStyle.class);
            fail("Should have failed");
        } catch (MismatchedInputException e) {
//            verifyException(e, "Cannot construct instance of");
            verifyException(e, "Unrecognized field");
        }
        JacksonXmlModule module = new JacksonXmlModule();
        module.setXMLTextElementName("value");
        XmlMapper mapper = new XmlMapper(module);
        JAXBStyle pojo = mapper.readValue(XML, JAXBStyle.class);
        assertEquals("foo", pojo.value);
    }

    // [dataformat-xml#66], implicit property from "XmlText"
    public void testIssue66() throws Exception
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper mapper = new XmlMapper(module);
        final String XML = "<Issue66Bean id=\"id\">text</Issue66Bean>";

        // let's start with deserialization
        Issue66Bean node = mapper.readValue(XML, Issue66Bean.class);
        assertEquals("id", node.id);
        assertEquals("text", node.textValue);

        // Let's serialize too
        String json = mapper.writeValueAsString(node);
        assertEquals(XML, json);
    }

    // [dataformat-xml#72]
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
