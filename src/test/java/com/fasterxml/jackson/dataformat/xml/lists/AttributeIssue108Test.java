package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class AttributeIssue108Test extends XmlTestBase
{
    static class Foo {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Bar> firstBar = new ArrayList<Bar>();
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Bar> secondBar = new ArrayList<Bar>();
    }

    static class Bar {
        public String value;

        @JacksonXmlProperty(isAttribute = true)
        public int id;
    }

    public void testIdsFromAttributes() throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        Foo foo = new Foo();
        Bar bar1 = new Bar();
        bar1.id = 1;
        bar1.value = "FIRST";
        foo.firstBar.add(bar1);
        Bar bar2 = new Bar();
        bar2.value = "SECOND";
        bar2.id = 2;
        foo.secondBar.add(bar2);
        String string = xmlMapper.writeValueAsString(foo);
        Foo fooRead = xmlMapper.readValue(string, Foo.class);
        assertEquals(foo.secondBar.get(0).id, fooRead.secondBar.get(0).id);
    }
}
