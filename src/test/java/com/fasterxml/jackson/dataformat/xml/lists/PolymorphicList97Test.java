package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

/**
 * @author pgelinas
 */
public class PolymorphicList97Test extends XmlTestBase
{
    @JsonTypeInfo(property = "type", use = Id.NAME)
    public static abstract class Foo {
        @JacksonXmlProperty(isAttribute = true)
        public String data;
    }

    @JsonTypeName("good")
    public static class FooGood extends Foo {
        public String bar;
    }

    @JsonTypeName("bad")
    public static class FooBad extends Foo {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> bar;
    }

    @Test
    public void testGood() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.registerSubtypes(FooGood.class);

        String xml = "<Foo type=\"good\" data=\"dummy\"><bar>FOOBAR</bar></Foo>";
        Foo fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooGood.class));

        xml = "<Foo data=\"dummy\" type=\"good\" ><bar>FOOBAR</bar></Foo>";
        fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooGood.class));
    }
    
    @Test
    public void testBad() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.registerSubtypes(FooBad.class);

        String xml = "<Foo type=\"bad\" data=\"dummy\"><bar><bar>FOOBAR</bar></bar></Foo>";
        Foo fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooBad.class));

        xml = "<Foo data=\"dummy\" type=\"bad\"><bar><bar>FOOBAR</bar></bar></Foo>";
        fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooBad.class));
    }
}
