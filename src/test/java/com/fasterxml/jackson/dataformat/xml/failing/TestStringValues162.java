package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TestStringValues162 extends XmlTestBase
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
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    public void testEmptyString162() throws Exception
    {
        Name name = MAPPER.readValue("<name><first>Ryan</first><last></last></name>",
                Name.class);
        assertNotNull(name);
        assertEquals("Ryan", name.first);
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

    public void testStringsInList() throws Exception
    {
        Names input = new Names();
        input.names.add(new Name("Bob", "Lee"));
        input.names.add(new Name("", ""));
        input.names.add(new Name("Sponge", "Bob"));
        String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        
//System.err.println("XML:\n"+xml);

        Names result = MAPPER.readValue(xml, Names.class);
        assertNotNull(result);
        assertNotNull(result.names);
        assertEquals(3, result.names.size());
        assertEquals("Bob", result.names.get(2).last);

        // [dataformat-xml#162]: should get empty String, not null
        assertEquals("", result.names.get(1).first);
    }
}
