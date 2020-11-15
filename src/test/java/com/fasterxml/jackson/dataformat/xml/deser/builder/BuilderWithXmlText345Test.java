package com.fasterxml.jackson.dataformat.xml.deser.builder;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class BuilderWithXmlText345Test extends XmlTestBase
{
    @JsonRootName("example")
    @JsonDeserialize(builder = Example345.ExampleBuilder.class)
    public static class Example345 {
        String name;

        String value;

        public Example345(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static ExampleBuilder builder() {
            return new ExampleBuilder();
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getName() {
            return name;
        }

        @JacksonXmlText
        public String getValue() {
            return value;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class ExampleBuilder {
            private String name;
            private String value;

            public Example345 build() {
                return new Example345(name, value);
            }

            public ExampleBuilder name(String name) {
                this.name = name;
                return this;
            }

            @JacksonXmlText
            public ExampleBuilder value(String value) {
                this.value = value;
                return this;
            }
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#345]
    public void testXmlTextViaBuilder345() throws Exception
    {
        Example345 in = Example345.builder()
            .name("name")
            .value("value")
            .build();

        String xml = MAPPER.writeValueAsString(in);
        Example345 out = MAPPER.readValue(xml, Example345.class);

        assertEquals(in.getName(), out.getName());
        assertEquals(in.getValue(), out.getValue());
        assertEquals(
            "<example name=\"name\">value</example>",
            xml
        );
    }
}
