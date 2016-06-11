package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.*;

// for [dataformat-xml#124]
public class EmptyListDeser124Test extends XmlTestBase
{
    public static class TestList {
        @JsonProperty("list")
        public List<Object> list;
    }

    // [dataformat-xml#124]
    public  void test124() throws Exception {
        final XmlMapper xmlMapper = new XmlMapper();
        TestList originalObject = new TestList();
        originalObject.list = new ArrayList<Object>();
        String xml = xmlMapper.writeValueAsString(originalObject);
//System.err.println(xml); // print <TestList><list/>></TestList>

        TestList result = xmlMapper.readValue(xml, TestList.class);

        assertNotNull(result.list);
        assertEquals(0, result.list.size());
    }
}
