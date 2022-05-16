package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// Tests for [dataformat-xml#525], related to relative order of "type"
// property (as attribute) compared to other properties
public class TypeInfoOrder525Test extends XmlTestBase
{
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ClassInfo525.class)
    })
    abstract static class TypeInfo525 {

    }

    @JsonTypeName("ClassInfo")
    static class ClassInfo525 extends TypeInfo525 {
        @JsonProperty List<ClassInfoElement525> element;
        @JsonProperty String name;
    }

    static class ClassInfoElement525 {
        @JsonProperty BindingInfo525 binding;
    }

    static class BindingInfo525 {
        @JsonProperty String description;
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = mapperBuilder()
            .defaultUseWrapper(false)
            .build();

    public void testTypeAfterOtherProperties() throws Exception {
        String xml =
                "  <typeInfo name=\"MyName\" type=\"ClassInfo\">\n" +
                "    <element>\n" +
                "      <binding description=\"Test\"/>\n" +
                "    </element>\n" +
                "  </typeInfo>";

        TypeInfo525 m = MAPPER.readValue(xml, TypeInfo525.class);
        assertEquals("MyName", ((ClassInfo525)m).name);
        assertEquals("Test", ((ClassInfo525)m).element.get(0).binding.description);
    }

    public void testTypeBeforeOtherProperties() throws Exception {
        String xml =
                "  <typeInfo type=\"ClassInfo\" name=\"MyName\">\n" +
                "    <element>\n" +
                "      <binding description=\"Test\"/>\n" +
                "    </element>\n" +
                "  </typeInfo>";

        TypeInfo525 m = MAPPER.readValue(xml, TypeInfo525.class);
        assertEquals("MyName", ((ClassInfo525)m).name);
        assertEquals("Test", ((ClassInfo525)m).element.get(0).binding.description);
    }
}
