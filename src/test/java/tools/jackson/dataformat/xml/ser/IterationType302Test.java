package tools.jackson.dataformat.xml.ser;

import java.util.*;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// [dataformat-xml#302] : Unable to serialize top-level Java8 Stream
public class IterationType302Test extends XmlTestBase
{
    private final ObjectMapper OBJECT_MAPPER = new XmlMapper();

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


    public void testCollectionSerialization() throws Exception {
        Collection<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");

        assertEquals("<ArrayList><item>a</item><item>b</item></ArrayList>",
            OBJECT_MAPPER.writeValueAsString(list));
    }

    public void testListSerialization() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        assertEquals("<ArrayList><item>a</item><item>b</item></ArrayList>",
            OBJECT_MAPPER.writeValueAsString(list));
    }

    public void testListIteratorSerialization() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        Iterator<String> listItr = list.iterator();

        assertEquals("<Itr><item>a</item><item>b</item></Itr>",
            OBJECT_MAPPER.writeValueAsString(listItr));
    }


    public void testStreamIteratorSerialization() throws Exception {
        assertEquals("<Adapter><item>a</item><item>b</item></Adapter>",
            OBJECT_MAPPER.writeValueAsString(Stream.of("a", "b").iterator()));
    }

    // [dataformat-xml#329] : Jackson ignores JacksonXmlElementWrapper on Stream
    public void testCollectionWrapperSerialization329() throws Exception {
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
    public void testIteratorWrapperSerialization329() throws Exception {
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

    // [dataformat-xml#148]
    public void testIteratorSerialization() throws Exception {
        assertEquals("<Bean148><list><item>2</item><item>1</item><item>0</item></list></Bean148>",
            OBJECT_MAPPER.writeValueAsString(new Bean148()).trim());
    }
}
