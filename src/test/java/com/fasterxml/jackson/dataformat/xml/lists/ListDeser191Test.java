package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ListDeser191Test extends XmlTestBase
{
    static class TestList {
        @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
        @JacksonXmlProperty(localName = "item")
        public ArrayList<ListItem> items;
    }    

    static class ListItem {
        @JacksonXmlProperty(isAttribute = true)
        public String name;
    }    

    public void testListDeser() throws Exception
    {
        ObjectMapper mapper = new XmlMapper();
        final String XML =
"<TestList>\n"+
"    <items>\n"+
"        <item name='Item1'/>\n"+
"        <item name='Item2'> </item>\n"+ // important: at least one ws char between start/end
"        <item name='Item3'/>\n"+
"    </items>\n"+
"</TestList>"
                ;
        TestList testList = mapper.readValue(XML, TestList.class);
        assertNotNull(testList);
        assertNotNull(testList.items);
        assertEquals(3, testList.items.size());
    }
}
