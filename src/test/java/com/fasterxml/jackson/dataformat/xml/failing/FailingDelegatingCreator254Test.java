package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class FailingDelegatingCreator254Test extends XmlTestBase
{
    static class Foo {
        public Bar bar;
    }

    static class Bar {
        Integer value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Bar(int i) {
            value = i;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    public void testIntList() throws Exception
    {
        Foo foo = MAPPER.readValue(
"<foo>\n" +
"   <bar>28</bar>\n" +
"</foo>", Foo.class);
        assertEquals(Integer.valueOf(28), foo.bar);
    }
}
