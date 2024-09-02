package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Unit test for [PullRequest#616], problems with filtered serialization.
 */
public class TestSerializationWithFilter extends XmlTestBase
{
    @JsonFilter("filter")
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

        XmlMapper xmlMapper = new XmlMapper();
        PropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception
            {
                if (include(writer) && writer.getName().equals("a")) {
                    int a = ((Item) pojo).a;
                    if (a <= 0){
                        return;
                    }
                }
                super.serializeAsField(pojo, jgen, provider, writer);
            }
        };
        FilterProvider filterProvider = new SimpleFilterProvider().addFilter("filter", filter);
        xmlMapper.setFilterProvider(filterProvider);
        xmlMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        String act = xmlMapper.writeValueAsString(bean);
        assertEquals(exp, act);
    }
}
