package com.fasterxml.jackson.dataformat.xml.lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        //@javax.xml.bind.annotation.XmlElementWrapper(name="stringList")
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

    private final XmlMapper MAPPER = new XmlMapper();
 
    public void testSimpleWrappedList() throws IOException
    {
        String xml = MAPPER.writeValueAsString(new ListBean(1, 2, 3));
        xml = removeSjsxpNamespace(xml);
        // 06-Dec-2010, tatu: Not completely ok; should default to not using wrapper...
        assertEquals("<ListBean><values><values>1</values><values>2</values><values>3</values></values></ListBean>", xml);
    }

    public void testStringList() throws IOException
    {
        StringListBean list = new StringListBean("a", "b", "c");
        String xml = MAPPER.writeValueAsString(list);
        xml = removeSjsxpNamespace(xml);
        // 06-Dec-2010, tatu: Not completely ok; should default to not using wrapper... but it's what we have now
        assertEquals("<StringListBean><stringList>"
                +"<strings><text>a</text></strings>"
                +"<strings><text>b</text></strings>"
                +"<strings><text>c</text></strings>"
                +"</stringList></StringListBean>", xml);
    }
}
