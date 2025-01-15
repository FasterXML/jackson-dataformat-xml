package tools.jackson.dataformat.xml.misc;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.exc.MismatchedInputException;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

public class TextValueTest extends XmlTestUtil
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
    
    @Test
    public void testSerializeAsText() throws IOException
    {
        String xml = MAPPER.writeValueAsString(new Simple());
        assertEquals("<Simple a=\"13\">something</Simple>", xml);
        // [dataformat-xml#56]: should work with indentation as well
        xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new Simple());
        assertEquals("<Simple a=\"13\">something</Simple>" + DEFAULT_NEW_LINE, xml);
    }

    @Test
    public void testDeserializeAsText() throws IOException
    {
        Simple result = MAPPER.readValue("<Simple a='99'>else</Simple>", Simple.class);
        assertEquals(99, result.a);
        assertEquals("else", result.text);
    }
    
    @Test
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
    @Test
    public void testAlternateTextElementName() throws IOException
    {
        final String XML = "<JAXBStyle>foo</JAXBStyle>";
        // first: verify that without change, POJO would not match:
        try {
            MAPPER.readerFor(JAXBStyle.class)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(XML);
            fail("Should have failed");
        } catch (MismatchedInputException e) {
//            verifyException(e, "Cannot construct instance of");
            verifyException(e, "Unrecognized property");
        }
        XmlMapper mapper = XmlMapper.builder()
                .nameForTextElement("value")
                .build();
        JAXBStyle pojo = mapper.readValue(XML, JAXBStyle.class);
        assertEquals("foo", pojo.value);
    }

    // [dataformat-xml#66], implicit property from "XmlText"
    @Test
    public void testIssue66() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .build();
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
    @Test
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
