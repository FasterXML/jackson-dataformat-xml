package com.fasterxml.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.*;

public class EmptyBeanDeser318Test extends XmlTestUtil
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

    // [dataformat-xml#579]
    static class Bean579 {
        public String str;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testMissing() throws Exception {
        String s = "<wrapper>"
                + "  <id>id</id>"
                + "</wrapper>";

        Wrapper value = MAPPER.readValue(s, Wrapper.class);
        assertEquals("id", value.id);
        assertNull(value.nested);
    }

    @Test
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

    // [dataformat-xml#579]
    @Test
    public void testEmptyRootElem579() throws Exception
    {
        Bean579 bean;

        ObjectReader R = MAPPER.readerFor(Bean579.class);

        // By default, no coercion of empty element
        bean = R.readValue("<Content/>");
        assertNotNull(bean);
        assertNull(bean.str);

        // So same as non-empty
        bean = R.readValue("<Content></Content>");
        assertNotNull(bean);
        assertNull(bean.str);

        // But enabling feature we can coerce POJO into null:

        // 29-May-2023, tatu: Alas! Note that we CANNOT use ObjectReader because
        //   FormatFeature (FromXmlParser.Feature) overrides ARE NOT APPLIED EARLY
        //   ENOUGH to take effect. Instead we must configure XmlMapper
       
        
        //R = R.with(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);

        R = mapperBuilder().enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                .build()
                .readerFor(Bean579.class);
        bean = R.readValue("<Content/>");
        assertNull(bean);

        // which won't affect non-empty variant
        bean = R.readValue("<Content></Content>");
        assertNotNull(bean);
        assertNull(bean.str);
    }
}
