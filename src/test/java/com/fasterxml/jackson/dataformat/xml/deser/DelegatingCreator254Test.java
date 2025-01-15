package com.fasterxml.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class DelegatingCreator254Test extends XmlTestUtil
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

    // [dataformat-xml#254]: Coercion needed for int-taking creator (as XML can
    // not natively detect scalars other than Strings)
    @Test
    public void testIntDelegatingCreator() throws Exception
    {
        Foo foo = MAPPER.readValue(
"<foo>\n" +
"   <bar>   28   </bar>\n" +
"</foo>", Foo.class);
        assertNotNull(foo.bar);
        assertEquals(Integer.valueOf(28), foo.bar.value);
    }
}
