package com.fasterxml.jackson.dataformat.xml.lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListSerializationTest extends XmlTestUtil
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
   
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();
 
    @Test
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

    @Test
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
}
