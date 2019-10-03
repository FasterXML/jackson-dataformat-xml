package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.testutil.NoCheckSubTypeValidator;

public class DefaultTyping325Test extends XmlTestBase
{
    static class Simple325 {
        protected String[] list;

        public String[] getList( ) { return list; }
        public void setList(String[] l) { list = l; }
    }

    // [dataformat-xml#325]
    public void testDefaultTypingWithInnerClass() throws IOException
    {
        ObjectMapper mapper = mapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT)
                .build();

        Simple325 s = new Simple325();
        s.setList(new String[] { "foo", "bar" });

        String doc = mapper.writeValueAsString(s);
        Simple325 result = mapper.readValue(doc, Simple325.class);
        assertNotNull(result.list);
        assertEquals(2, result.list.length);
    }
}
