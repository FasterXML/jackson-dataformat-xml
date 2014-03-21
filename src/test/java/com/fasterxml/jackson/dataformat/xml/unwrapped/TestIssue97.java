/*
 * Copyright (c) 2002-2014 Nu Echo Inc. All rights reserved.
 */

package com.fasterxml.jackson.dataformat.xml.unwrapped;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

/**
 * @author Nu Echo Inc.
 */
@RunWith(JUnit4.class)
public class TestIssue97 {

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

        // BOOM
        xml = "<Foo data=\"dummy\" type=\"bad\"><bar><bar>FOOBAR</bar></bar></Foo>";
        fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooBad.class));
    }

    @JsonTypeInfo(property = "type", use = Id.NAME)
    public static abstract class Foo {
        private String mData;

        public void setData(String data) {
            mData = data;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getData() {
            return mData;
        }
    }

    @JsonTypeName("good")
    public static class FooGood extends Foo {
        private String mBar;

        public String getBar() {
            return mBar;
        }

        public void setBar(String bar) {
            mBar = bar;
        }
    }

    @JsonTypeName("bad")
    public static class FooBad extends Foo {
        private List<String> mBar;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> getBar() {
            return mBar;
        }

        public void setBar(List<String> bar) {
            mBar = bar;
        }
    }
}
