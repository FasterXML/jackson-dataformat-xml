package com.fasterxml.jackson.dataformat.xml.lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class ListSerializationTest extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */
    static class ListBean
    {
        public final List<Integer> values = new ArrayList<Integer>();

        public ListBean() { }
        public ListBean(int... ints) {
            for (int i : ints) {
                values.add(Integer.valueOf(i));
            }
        }
    }

    static class StringListBean
    {
        // to see what JAXB gives, uncomment:
        //@jakarta.xml.bind.annotation.XmlElementWrapper(name="stringList")
        @JacksonXmlElementWrapper(localName="stringList")
        public List<StringBean> strings;
        
        public StringListBean() { strings = new ArrayList<StringBean>(); }
        public StringListBean(String... texts)
        {
            strings = new ArrayList<StringBean>();
            for (String text : texts) {
                strings.add(new StringBean(text));
            }
        }
    }

    // [dataformat-xml#148]
    static class Bean148 {
        @JsonProperty("item")
        @JacksonXmlElementWrapper(localName = "list")
        public Iterator<String> items() {
          return new Iterator<String>() {
            int item = 3;

            @Override
            public boolean hasNext() {
              return item > 0;
            }

            @Override
            public String next() {
              item--;
              return Integer.toString(item);
            }
          };
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();
 
    public void testSimpleWrappedList() throws IOException
    {
        String xml = MAPPER.writeValueAsString(new ListBean(1, 2, 3));
        xml = removeSjsxpNamespace(xml);
        assertEquals("<ListBean><values><values>1</values><values>2</values><values>3</values></values></ListBean>", xml);

        // for [dataformat-xml#469] try forcing wrapping:
        XmlMapper unwrapMapper = XmlMapper.builder()
                .annotationIntrospector(new JacksonXmlAnnotationIntrospector(false))
                .build();
        xml = unwrapMapper.writeValueAsString(new ListBean(1, 2, 3));
        xml = removeSjsxpNamespace(xml);
        assertEquals("<ListBean>"
                +"<values>1</values><values>2</values><values>3</values>"
                +"</ListBean>",
                xml);
    }

    public void testStringList() throws IOException
    {
        StringListBean list = new StringListBean("a", "b", "c");
        String xml = MAPPER.writeValueAsString(list);
        xml = removeSjsxpNamespace(xml);
        assertEquals("<StringListBean><stringList>"
                +"<strings><text>a</text></strings>"
                +"<strings><text>b</text></strings>"
                +"<strings><text>c</text></strings>"
                +"</stringList></StringListBean>", xml);
    }

    // [dataformat-xml#148]
    public void testIteratorSerialization() throws Exception
    {
        assertEquals("<Bean148><item>2</item><item>1</item><item>0</item></Bean148>",
                MAPPER.writeValueAsString(new Bean148()).trim());
    }
}
