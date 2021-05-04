package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class EmptyStringValueTest extends XmlTestBase
{
    static class Name {
        public String first;
        public String last;

        public Name() { }
        public Name(String f, String l) {
            first = f;
            last = l;
        }
    }

    static class Names {
        public List<Name> names = new ArrayList<Name>();
    }

    // [dataformat-xml#25]
    static class EmptyStrings25
    {
        @JacksonXmlProperty(isAttribute=true)
        public String a = "NOT SET";
        public String b = "NOT SET";
    }

    // [dataformat-xml#427]
    static class Stuff427 {
        String str;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Stuff427(String s) { str = s; }
    }

    static class Product427 {
        Stuff427 stuff;

        public Product427(@JsonProperty("stuff") Stuff427 s) { stuff = s; }
    }
    
    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testEmptyString162() throws Exception
    {
        Name name = MAPPER.readValue("<name><first>Ryan</first><last></last></name>",
                Name.class);
        assertNotNull(name);
        assertEquals("Ryan", name.first);
        assertEquals("", name.last);
    }

    public void testEmptyElement() throws Exception
    {
        final String XML = "<name><first/><last></last></name>";

        // Default settings (since 2.12): empty element does NOT become `null`:
        Name name = MAPPER.readValue(XML, Name.class);
        assertNotNull(name);
        assertEquals("", name.first);
        assertEquals("", name.last);

        // but can be changed
        XmlMapper mapper2 = XmlMapper.builder()
                .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                .build();
        name = mapper2.readValue(XML, Name.class);
        assertNotNull(name);
        assertNull(name.first);
        assertEquals("", name.last);
    }

    public void testEmptyStringElement() throws Exception
    {
        // then with empty element
        StringBean bean = MAPPER.readValue("<StringBean><text></text></StringBean>", StringBean.class);
        assertNotNull(bean);
        // empty String or null?
        // As per [dataformat-xml#162], really should be "", not null:
        assertEquals("", bean.text);
//        assertNull(bean.text);
    }

    // [dataformat-xml#25]
    public void testEmptyStringFromElemAndAttr() throws Exception
    {
        EmptyStrings25 ob = MAPPER.readValue("<EmptyString a=''><b /></EmptyString>",
                EmptyStrings25.class);
        assertNotNull(ob);
        assertEquals("", ob.a);
        assertEquals("", ob.b);
    }

    // [dataformat-xml#427]
    public void testEmptyIssue427() throws Exception
    {
        String xml = "<product><stuff></stuff></product>";

        Product427 product = MAPPER.readValue(xml, Product427.class);

        assertNotNull(product);
        assertNotNull(product.stuff);
        assertEquals("", product.stuff.str);
    }
}
