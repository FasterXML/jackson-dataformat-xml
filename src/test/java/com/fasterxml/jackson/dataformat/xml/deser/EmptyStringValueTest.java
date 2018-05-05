package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

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

    /*
    /**********************************************************
    /* Test methods
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

    public void testEmptyElement() throws Exception
    {
        final String XML = "<name><first/><last></last></name>";
        // 04-May-2018, tatu: With Jackson 3.x, default is now to get "" for
        //   empty tags
        Name name = MAPPER.readValue(XML, Name.class);
        assertNotNull(name);
        assertEquals("", name.first);
        assertEquals("", name.last);

        // but can be changed
        XmlMapper mapper2 = newMapperBuilder()
                .withConfigOverride(String.class,
                        o -> o.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.SET)))
            .build();
        name = mapper2.readValue(XML, Name.class);
        assertNotNull(name);
        assertNull(name.first);
        assertNull(name.last);
    }

    public void testEmptyStringElement() throws Exception
    {
        // then with empty element
        StringBean bean = MAPPER.readValue("<StringBean><text></text></StringBean>", StringBean.class);
        assertNotNull(bean);
        // empty String or null?
        // As per [dataformat-xml#162], really should be "", not null:
        assertEquals("", bean.text);
    }
}
