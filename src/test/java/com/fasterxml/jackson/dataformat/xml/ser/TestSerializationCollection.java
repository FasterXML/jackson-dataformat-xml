package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestSerializationCollection extends XmlTestBase {


    @JacksonXmlRootElement(localName="person", namespace="http://example.org/person", wrapperForIndexedType = "Person")
    static class Person {

        @JacksonXmlProperty(isAttribute = true)
        public Integer id;

        public String n;

        public Person(Integer id, String name) {
            this.id = id;
            this.n = name;
        }
    }

    @JacksonXmlRootElement(localName = "persons")
    static class PersonList extends ArrayList<Person>{}

    public void testList() throws Exception
    {
        List<String> personNames = Arrays.asList("A", "B", "C");
        PersonList personList = IntStream.range(0, personNames.size())
                .mapToObj(count -> new Person(count, personNames.get(count)))
                .collect(Collectors.toCollection(PersonList::new));
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(personList);
        assertEquals("<persons><Person id=\"0\"><n>A</n></Person><Person id=\"1\"><n>B</n></Person><Person id=\"2\"><n>C</n></Person></persons>",
                xml);
    }
}
