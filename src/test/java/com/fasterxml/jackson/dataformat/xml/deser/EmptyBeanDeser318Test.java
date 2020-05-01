package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class EmptyBeanDeser318Test extends XmlTestBase
{
    static class Wrapper {
        @JacksonXmlProperty(localName = "id")
        String id;
        @JacksonXmlProperty(localName = "nested")
        Nested nested;
    }

    static class Nested {
        @JacksonXmlProperty(localName = "nested2")
        Nested2 nested2;
    }

    static class Nested2 {
        @JacksonXmlProperty(localName = "attr", isAttribute = true)
        String attr;
        @JacksonXmlText
        String value;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testEmptyString() throws Exception {
        String s = "<wrapper>"
                + "  <id>id</id>"
                + "  <nested></nested>"
                + "</wrapper>";

        Wrapper value = MAPPER.readValue(s, Wrapper.class);
        assertEquals("id", value.id);
        assertNotNull(value.nested);
        assertNull(value.nested.nested2);
    }

    public void testBlankString() throws Exception {
        String s = "<wrapper>"
                + "  <id>id</id>"
                + "  <nested>    </nested>"
                + "</wrapper>";

        // This fails with the following exception:
        // com.fasterxml.jackson.databind.exc.MismatchedInputException:
        // Cannot construct instance of `JacksonXMLTest$Nested` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('    ')
        Wrapper value = MAPPER.readValue(s, Wrapper.class);
        assertEquals("id", value.id);
        assertNotNull(value.nested);
        assertNull(value.nested.nested2);
    }

    public void testBlankString2() throws Exception {
        String s = "<wrapper>"
                + "  <id>id</id>"
                + "  <nested>    </nested>"
                + "</wrapper>";

        Wrapper value = MAPPER.readerFor(Wrapper.class)
                .readValue(s);
        assertEquals("id", value.id);
        assertNotNull(value.nested);
        assertNull(value.nested.nested2);
    }

    public void testMissing() throws Exception {
        String s = "<wrapper>"
                + "  <id>id</id>"
                + "</wrapper>";

        Wrapper value = MAPPER.readValue(s, Wrapper.class);
        assertEquals("id", value.id);
        assertNull(value.nested);
    }

    public void testValidStructure() throws Exception {
        String s = "<wrapper>"
                + "  <id>id</id>"
                + "  <nested>"
                + "    <nested2 attr=\"test\"><![CDATA[Some text]]></nested2>"
                + "  </nested>"
                + "</wrapper>";

        Wrapper value = MAPPER.readValue(s, Wrapper.class);
        assertEquals("id", value.id);
        assertEquals("test", value.nested.nested2.attr);
        assertEquals("Some text", value.nested.nested2.value);
    }
}
