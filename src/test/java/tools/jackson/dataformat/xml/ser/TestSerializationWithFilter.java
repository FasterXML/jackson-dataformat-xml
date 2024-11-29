package tools.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.core.JsonGenerator;

import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.PropertyFilter;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Unit test for [PullRequest#616], problems with filtered serialization.
 */
public class TestSerializationWithFilter extends XmlTestBase
{
    @JsonFilter("filter")
    @JsonPropertyOrder({ "b", "c" })
    static class Item
    {
        @JacksonXmlText
        public int a;
        public int b;
        public int c;
    }

    public void testPullRequest616() throws Exception
    {
        Item bean = new Item();
        bean.a = 0;
        bean.b = 10;
        bean.c = 100;

        String exp = "<Item><b>10</b><c>100</c></Item>";

        PropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            public void serializeAsProperty(Object pojo, JsonGenerator g,
                    SerializationContext ctxt, PropertyWriter writer)
                throws Exception
            {
                if (include(writer) && writer.getName().equals("a")) {
                    int a = ((Item) pojo).a;
                    if (a <= 0)
                        return;
                }
                super.serializeAsProperty(pojo, g, ctxt, writer);
            }
        };
        FilterProvider filterProvider = new SimpleFilterProvider().addFilter("filter", filter);
        XmlMapper xmlMapper = XmlMapper.builder()
                .filterProvider(filterProvider)
                .build();
        assertEquals(exp, xmlMapper.writeValueAsString(bean));
    }
}
