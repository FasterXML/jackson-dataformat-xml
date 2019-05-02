package com.fasterxml.jackson.dataformat.xml.failing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.testutil.NoCheckSubTypeValidator;

public class DefaultTyping325Test extends XmlTestBase
{
    static class Simple {
        protected List<String> list;

        public List<String> getList( ) { return list; }
        public void setList(List<String> l) { list = l; }
    }

    public void testCanSerialize() throws IOException
    {
        ObjectMapper mapper = mapperBuilder()
                .enableDefaultTyping(NoCheckSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT)
                .build();

        // construct test object
        Simple s = new Simple();
        s.setList(Arrays.asList("foo", "bar"));

        String doc = mapper.writeValueAsString(s);
        Simple result = mapper.readValue(doc, Simple.class);
        assertNotNull(result.list);
        assertEquals(2, result.list.size());
    }
}
