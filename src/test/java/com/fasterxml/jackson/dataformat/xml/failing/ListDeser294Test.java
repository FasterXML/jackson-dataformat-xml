package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class ListDeser294Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "levels")
    static class RootLevel {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "sublevel")
        public List<Sublevel> sublevels = new ArrayList<>();
    }

    @JsonPropertyOrder({ "id", "sublevel" }) // fails
//    @JsonPropertyOrder({ "sublevel", "id" }) // works
    static class Sublevel {
        public Integer id;
        public String sublevel;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testNestedLists294() throws Exception
    {
        RootLevel tree = new RootLevel();
        tree.sublevels.add(_newSublevel(1, "Name A"));
        tree.sublevels.add(_newSublevel(2, "Name B"));
        String xml = MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(tree);
//System.err.println("XML:\n"+xml);
        RootLevel resTree = MAPPER.readValue(xml, RootLevel.class);
        assertNotNull(resTree);
        assertNotNull(resTree.sublevels);
        assertEquals(2, resTree.sublevels.size());
        assertEquals("Name B", resTree.sublevels.get(1).sublevel);
    }

    private Sublevel _newSublevel(Integer id, String sublevel) {
        Sublevel res = new Sublevel();
        res.id = id;
        res.sublevel = sublevel;
        return res;
    }
}
