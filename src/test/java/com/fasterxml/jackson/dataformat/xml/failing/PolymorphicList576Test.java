package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class PolymorphicList576Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "wrapper")
    static class Wrapper extends Base {

        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Item> items = new ArrayList<>();

        public Wrapper(List<Item> items) {
            this.items = items;
        }

        public Wrapper() {
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }



        @Override
        public String toString() {
            return "Wrapper{" +
                    "items=" + items +
                    '}';
        }
    }

    @JacksonXmlRootElement(localName = "item")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Item {

        private String id;

        public Item(String id) {
            this.id = id;
        }

        public Item() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Wrapper.class, name = "wrapper")
    })
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Base {
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper XML_MAPPER = newMapper();

    public void test_3itemsInXml_expect_3itemsInDeserializedObject() throws Exception {
        String xmlString = 
                "<?xml version='1.0' encoding='UTF-8'?>\n"
                +"<wrapper type='wrapper'>\n"
                +" <item><id>1</id></item>\n"
                +" <item><id>2</id></item>\n"
                +" <item><id>3</id></item>\n"
                +"</wrapper>\n"
                ;
        Base base = XML_MAPPER.readValue(xmlString, Base.class);
        assertEquals(3, ((Wrapper)base).getItems().size());
    }

    public void test_2itemsInObject_expect_2itemsInObjectAfterRoundTripDeserializationToBaseClass() throws Exception {
        Wrapper wrapper = new Wrapper();
        Item item1 = new Item("1");
        Item item2 = new Item("2");
        wrapper.setItems(Arrays.asList(item1, item2));

        String writeValueAsString = XML_MAPPER.writeValueAsString(wrapper);
        Base base = XML_MAPPER.readValue(writeValueAsString, Base.class);

        assertEquals(2, ((Wrapper)base).getItems().size());
    }
}
