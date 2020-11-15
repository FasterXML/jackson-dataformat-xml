package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PolymorphicSerialization389Test extends XmlTestBase
{
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ConcreteModel.class, name = "ConcreteModel")
    })
    public abstract class AbstractModel {
        
         @JacksonXmlProperty(isAttribute = true)
         public Long id;
         
         @JacksonXmlProperty(isAttribute = true)
         public String name;
    }

    @JsonRootName("Concrete")
    public class ConcreteModel extends AbstractModel {
         @JacksonXmlProperty(isAttribute = true)
         public String someAdditionalField;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#389]
    public void testIssue389() throws Exception
    {
        ConcreteModel concreteModel = new ConcreteModel();
        concreteModel.id = 1L;
        concreteModel.name = "Bob";
        concreteModel.someAdditionalField = "...";

        String xml1 = MAPPER.writeValueAsString(concreteModel);
        String xml2 = MAPPER.writerFor(ConcreteModel.class)
                .writeValueAsString(concreteModel);
        assertEquals(xml1, xml2);
    }
}
