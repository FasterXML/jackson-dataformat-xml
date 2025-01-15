package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolymorphicSerialization389Test extends XmlTestUtil
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
    @Test
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
