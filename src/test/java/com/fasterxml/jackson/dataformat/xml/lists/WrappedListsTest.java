package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class WrappedListsTest extends XmlTestBase
{
    static class Order  {
        @JacksonXmlElementWrapper(localName = "line_items")
        @JacksonXmlProperty(localName = "item")  
        private List<ListItem> line_items; // new ArrayList<ListItem>();
    }

    static class ListItem {
        public int id;
        
        public ListItem(int id) { this.id = id; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = xmlMapper(true);

    // For [Issue#103]
    public void testEmptyList() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Order());
        assertEquals("<Order/>", xml);
        // If we expected Empty list, it'd be:
//        assertEquals("<Order><line_items/></Order>", xml);
    }
}
