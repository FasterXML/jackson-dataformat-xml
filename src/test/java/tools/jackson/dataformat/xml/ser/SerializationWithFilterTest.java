package tools.jackson.dataformat.xml.ser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.core.JsonGenerator;

import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.*;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for [PullRequest#616], problems with filtered serialization.
 */
public class SerializationWithFilterTest extends XmlTestUtil
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

    // [databind#6136]: any-getter entries must be filtered per entry
    @JsonFilter("filter")
    static class AnyBean
    {
        public String name = "bob";

        @JsonAnyGetter
        public Map<String, String> anyProperties() {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("a", "1");
            m.put("secret", "s3cr3t");
            return m;
        }
    }

    @Test
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

    // [databind#6136]
    @Test
    public void anyGetterWithExcludingFilter() throws Exception
    {
        XmlMapper xmlMapper = XmlMapper.builder()
                .filterProvider(new SimpleFilterProvider().addFilter("filter",
                        SimpleBeanPropertyFilter.serializeAllExcept("secret")))
                .build();
        assertEquals("<AnyBean><name>bob</name><a>1</a></AnyBean>",
                xmlMapper.writeValueAsString(new AnyBean()));
    }

    // [databind#6136]
    @Test
    public void anyGetterWithIncludingFilter() throws Exception
    {
        XmlMapper xmlMapper = XmlMapper.builder()
                .filterProvider(new SimpleFilterProvider().addFilter("filter",
                        SimpleBeanPropertyFilter.filterOutAllExcept("name", "a")))
                .build();
        assertEquals("<AnyBean><name>bob</name><a>1</a></AnyBean>",
                xmlMapper.writeValueAsString(new AnyBean()));
    }
}
