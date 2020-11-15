package com.fasterxml.jackson.dataformat.xml.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyName;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// NOTE: even tho `@JacksonXmlRootElement` will be deprecated in near
// future (possibly in 2.13) -- to be replaced by `@JsonRootName` -- this
// test will use it to ensure we handle both annotations as expected
public class RootNameTest extends XmlTestBase
{
    static class RootBeanBase
    {
        public String value;

        protected RootBeanBase() { this("123"); }
        public RootBeanBase(String v) {
            value = v;
        }
    }

    @JacksonXmlRootElement(localName="root")
    static class RootBean extends RootBeanBase
    {
        protected RootBean() { super(); }
    }

    @JacksonXmlRootElement(localName="nsRoot", namespace="http://foo")
    static class NsRootBean
    {
        public String value = "abc";
    }

    @SuppressWarnings("serial")
    @JacksonXmlRootElement(localName="TheStrings")
    static class StringList extends ArrayList<String> {
        public StringList(String...strings) {
            addAll(Arrays.asList(strings));
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    protected XmlMapper _xmlMapper = new XmlMapper();

    // Unit test to verify that root name is properly set
    public void testRootNameAnnotation() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new StringBean());
        
        // Hmmh. Looks like JDK Stax may adds bogus ns declaration. As such,
        // let's just check that name starts ok...
        if (!xml.startsWith("<StringBean")) {
            fail("Expected root name of 'StringBean'; but XML document is ["+xml+"]");
        }

        // and then see that basic non-namespace root is ok
        xml = _xmlMapper.writeValueAsString(new RootBean());
        assertEquals("<root><value>123</value></root>", xml);

        // and namespace one too
        xml = _xmlMapper.writeValueAsString(new NsRootBean());
        if (xml.indexOf("nsRoot") < 0) { // verify localName
            fail("Expected root name of 'nsRoot'; but XML document is ["+xml+"]");
        }
        // and NS declaration
        if (xml.indexOf("http://foo") < 0) {
            fail("Expected NS declaration for 'http://foo', not found, XML document is ["+xml+"]");
        }
    }

    public void testDynamicRootName() throws IOException
    {
        String xml;

        ObjectWriter w = _xmlMapper.writer().withRootName(PropertyName.construct("rudy", "localhost"));

        xml = w.writeValueAsString(new StringBean("foo"));
        assertEquals("<rudy xmlns=\"localhost\"><text xmlns=\"\">foo</text></rudy>", xml);

        xml = w.writeValueAsString(new StringBean(null));
        assertEquals("<rudy xmlns=\"localhost\"><text xmlns=\"\"/></rudy>", xml);

        // and even with null will respect configured root name
        xml = w.writeValueAsString(null);
        assertEquals("<rudy xmlns=\"localhost\"/>", xml);
    }

    public void testDynamicRootNameForList() throws IOException
    {
        String xml;

        // First: using explicit root name override, simple String
        xml = _xmlMapper.writer().withRootName("Listy")
                .writeValueAsString(Arrays.asList("abc", "def"));
        assertEquals("<Listy><item>abc</item><item>def</item></Listy>", xml);

        // Second: using explicit root name override, with namespace
        xml = _xmlMapper.writer().withRootName(PropertyName.construct("Spaced", "http://foo"))
                .writeValueAsString(Arrays.asList("foo", "bar"));
        assertEquals("<Spaced xmlns=\"http://foo\"><item>foo</item><item>bar</item></Spaced>", xml);

        // Third: root name annotation
        xml = _xmlMapper.writer()
                .writeValueAsString(new StringList("a", "b"));
        assertEquals("<TheStrings><item>a</item><item>b</item></TheStrings>", xml);
    }
}
