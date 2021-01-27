package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UntypedObjectDeserializer445Test extends XmlTestBase {

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();


    public void testDuplicateListDeserialization() throws Exception {
        final String XML =
                "<person>\n" +
                        "    <name>a</name>\n" +
                        "    <name>b</name>\n" +
                        "    <surname>c</surname>\n" +
                        "    <surname>d</surname>\n" +
                        "</person>";
        @SuppressWarnings("unchecked")
        Map<String, List<String>> person = (Map<String, List<String>>) MAPPER.readValue(XML, Object.class);
        List<String> names = person.get("name");
        List<String> surnames = person.get("surname");
        assertEquals(2, names.size());
        assertTrue(names.containsAll(Arrays.asList("a", "b")));
        assertEquals(2, surnames.size());
        assertTrue(surnames.containsAll(Arrays.asList("c", "d")));
    }
}
