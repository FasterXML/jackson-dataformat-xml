package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

// [dataformat-xml#302] : Unable to serialize top-level Java8 Stream
public class Jdk8StreamSerialization302Test extends XmlTestBase {

    final ObjectMapper OBJECT_MAPPER = new XmlMapper();

    public static class StreamWrapper329 {
        private Stream<String> data;

        @JacksonXmlElementWrapper(localName = "elements")
        @JacksonXmlProperty(localName = "element")
        public Stream<String> getData() {
            return data;
        }

        public void setData(Stream<String> data) {
            this.data = data;
        }
    }

    public static class CollectionWrapper329 {
        private Collection<String> data;

        @JacksonXmlElementWrapper(localName = "elements")
        @JacksonXmlProperty(localName = "element")
        public Collection<String> getData() {
            return data;
        }

        public void setData(Collection<String> data) {
            this.data = data;
        }
    }

    public static class IteratorWrapper329 {
        private Iterator<String> data;

        @JacksonXmlElementWrapper(localName = "elements")
        @JacksonXmlProperty(localName = "element")
        public Iterator<String> getData() {
            return data;
        }

        public void setData(Iterator<String> data) {
            this.data = data;
        }
    }

    public void testCollectionSerialization() throws JsonProcessingException {
        Collection<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");

        assertEquals("<ArrayList><item>a</item><item>b</item></ArrayList>",
            OBJECT_MAPPER.writeValueAsString(list));
    }

    public void testListSerialization() throws JsonProcessingException {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        assertEquals("<ArrayList><item>a</item><item>b</item></ArrayList>",
            OBJECT_MAPPER.writeValueAsString(list));
    }

    public void testListIteratorSerialization() throws JsonProcessingException {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        Iterator<String> listItr = list.iterator();

        assertEquals("<Itr><item>a</item><item>b</item></Itr>",
            OBJECT_MAPPER.writeValueAsString(listItr));
    }


    public void testStreamIteratorSerialization() throws JsonProcessingException {
        assertEquals("<Adapter><item>a</item><item>b</item></Adapter>",
            OBJECT_MAPPER.writeValueAsString(Stream.of("a", "b").iterator()));
    }

    // [dataformat-xml#329] : Jackson ignores JacksonXmlElementWrapper on Stream
    public void testCollectionWrapperSerialization329() throws JsonProcessingException {
        Collection<String> collection = new ArrayList<>();
        collection.add("a");
        collection.add("b");
        CollectionWrapper329 wrapper = new CollectionWrapper329();
        wrapper.setData(collection);

        assertEquals(
            "<CollectionWrapper329><elements>" +
                "<element>a</element>" +
                "<element>b</element>" +
                "</elements></CollectionWrapper329>",
            OBJECT_MAPPER.writeValueAsString(wrapper));
    }

    // [dataformat-xml#329] : Jackson ignores JacksonXmlElementWrapper on Stream
    public void testIteratorWrapperSerialization329() throws JsonProcessingException {
        Collection<String> collection = new ArrayList<>();
        collection.add("a");
        collection.add("b");
        IteratorWrapper329 wrapper = new IteratorWrapper329();
        wrapper.setData(collection.iterator());

        assertEquals(
            "<IteratorWrapper329><elements>" +
                "<element>a</element>" +
                "<element>b</element>" +
                "</elements></IteratorWrapper329>",
            OBJECT_MAPPER.writeValueAsString(wrapper));
    }
    
    /*
    /**********************************************************
    /* Stream tests, requires Jdk8Module
    /**********************************************************

    public void testStreamSerialization() throws JsonProcessingException {
        String jsonStr = OBJECT_MAPPER.writeValueAsString(Stream.of("a", "b"));
        assertEquals("<Head><item>a</item><item>b</item></Head>", jsonStr);
    }

    // [dataformat-xml#329] : Jackson ignores JacksonXmlElementWrapper on Stream
    public void testStreamWrapperSerialization329() throws JsonProcessingException {
        StreamWrapper329 wrapper = new StreamWrapper329();
        wrapper.setData(Stream.of("a", "b"));

        assertEquals(
            "<StreamWrapper329><elements>" +
                "<element>a</element>" +
                "<element>b</element>" +
                "</elements></StreamWrapper329>",
            OBJECT_MAPPER.writeValueAsString(wrapper));
    }
    */
}
