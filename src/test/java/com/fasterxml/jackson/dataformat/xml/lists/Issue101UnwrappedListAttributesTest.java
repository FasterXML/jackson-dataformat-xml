package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

// Failing unit test(s) wrt [Issue#64]
public class Issue101UnwrappedListAttributesTest extends XmlTestBase
{
    // For [dataformat-xml#101]
    @JsonRootName("root")    
    @JsonPropertyOrder({ "unwrapped", "name" })
    static class Root {
        @JacksonXmlProperty(localName = "unwrapped")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<UnwrappedElement> unwrapped;

        public String name;
    }
     @JsonPropertyOrder({ "id", "type" })
     static class UnwrappedElement {
        public UnwrappedElement () {}

        public UnwrappedElement (String id, String type) {
            this.id = id;
            this.type = type;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String id;

        @JacksonXmlProperty(isAttribute = true)
        public String type;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#101]
    public void testWithTwoAttributes() throws Exception
    {
        final String EXP = "<root>"
                +"<unwrapped id=\"1\" type=\"string\"/>"
                +"<unwrapped id=\"2\" type=\"string\"/>"
                +"<name>test</name>"
                +"</root>";
        Root rootOb = new Root();
        rootOb.unwrapped = Arrays.asList(
                new UnwrappedElement("1", "string"),
                new UnwrappedElement("2", "string")
        );
        rootOb.name = "test";

        // First, serialize, which works
        String xml = MAPPER.writeValueAsString(rootOb);
        assertEquals(EXP, xml);

        // then try deserialize
        Root result = MAPPER.readValue(xml, Root.class);
        assertNotNull(result);
        assertEquals(rootOb.name, result.name);
    }
}
