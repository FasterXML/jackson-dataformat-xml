package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

public class ScratchUnwrapped903Test extends XmlTestUtil
{
    // @JacksonXmlText + CDATA -> goes through checkNextIsUnwrapped() branch
    static class TextBean {
        @JacksonXmlProperty(isAttribute = true)
        public String id = "x";

        @JacksonXmlText
        @JacksonXmlCData
        public String text;

        public TextBean() { }
        public TextBean(String t) { text = t; }
    }

    // CDATA on an attribute: flag should be ignored
    static class AttrBean {
        @JacksonXmlProperty(isAttribute = true)
        @JacksonXmlCData
        public String value;

        public AttrBean() { }
        public AttrBean(String v) { value = v; }
    }

    private final XmlMapper M = newMapper();

    @Test public void unwrappedText() throws Exception {
        for (String v : new String[] { "a]]>b", "]]>", "]]]>" }) {
            String xml = M.writeValueAsString(new TextBean(v));
            System.out.println("TEXT ["+v+"] -> "+xml);
            TextBean r = M.readValue(xml, TextBean.class);
            assertEquals(v, r.text, "xml: "+xml);
        }
    }

    @Test public void unwrappedTextPretty() throws Exception {
        for (String v : new String[] { "a]]>b", "]]>" }) {
            String xml = M.writerWithDefaultPrettyPrinter().writeValueAsString(new TextBean(v));
            System.out.println("TEXT-PRETTY ["+v+"] -> "+xml);
            TextBean r = M.readValue(xml, TextBean.class);
            assertEquals(v, r.text, "xml: "+xml);
        }
    }

    @Test public void attribute() throws Exception {
        String xml = M.writeValueAsString(new AttrBean("a]]>b"));
        System.out.println("ATTR -> "+xml);
        assertEquals("a]]>b", M.readValue(xml, AttrBean.class).value);
    }
}
