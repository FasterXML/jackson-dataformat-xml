package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

// Failing unit test(s) wrt [Issue#64]
public class Issue101UnwrappedListAttributesTest extends XmlTestBase
{
    static class Optional {
        @JacksonXmlText
        public String number = "NOT SET";

        @JacksonXmlProperty(isAttribute=true)
        public String type = "NOT SET";

        public Optional() { }
        
        // uncommenting this ALSO works:
//        public Optional(String n) { number = n; }
    }

    static class Optionals {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Optional> optional;
    } 

    // For [Issue#101]
    @JacksonXmlRootElement(localName = "root")    
    @JsonPropertyOrder({ "unwrapped", "name" })
    static class Root {
        @JacksonXmlProperty(localName = "unwrapped")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<UnwrappedElement> unwrapped;

        public String name;
    }

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

    // // [Issue#64]
    public void testOptionalsWithMissingType() throws Exception
    {
//        Optionals ob = MAPPER.readValue("<MultiOptional><optional type='work'>123-456-7890</optional></MultiOptional>",
        Optionals ob = MAPPER.readValue("<MultiOptional><optional>123-456-7890</optional></MultiOptional>",
                Optionals.class);
        assertNotNull(ob);
        assertNotNull(ob.optional);
        assertEquals(1, ob.optional.size());

//            System.err.println("ob: " + ob); // works fine

        Optional opt = ob.optional.get(0);
        assertEquals("123-456-7890", opt.number);
        assertEquals("NOT SET", opt.type);
    }

    // [Issue#101]
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
