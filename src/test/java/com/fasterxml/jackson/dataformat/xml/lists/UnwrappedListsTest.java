package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class UnwrappedListsTest extends XmlTestBase
{
    static class Value {
        public String v;

        public Value() { }
        public Value(String str) { v = str; }
    }

    @XmlRootElement(name="list")
    @JsonRootName("list")
    static class WrappedList {
        @XmlElementWrapper(name="WRAP")
        @JacksonXmlElementWrapper(localName = "WRAP")
        public Value[] value;
    }

    @XmlRootElement(name="list")
    @JsonRootName("list")
    static class UnwrappedList {
        @JacksonXmlElementWrapper(useWrapping=false)
        public Value[] value;
    }

    static class DefaultList {
        public Value[] value;
    }

    // [dataformat-xml#64]
    static class Optionals {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Optional> optional;
    } 

    static class Optional {
        @JacksonXmlText
        public String number = "NOT SET";

        @JacksonXmlProperty(isAttribute=true)
        public String type = "NOT SET";

        public Optional() { }
    }
    
    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testWrappedLists() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        WrappedList list = new WrappedList();
        list.value = new Value[] { new Value("a"), new Value("b") };

        // First, serialize:
        
        String json = mapper.writeValueAsString(list);
//      withJAXB(list);
        assertEquals("<list><WRAP><value><v>a</v></value><value><v>b</v></value></WRAP></list>", json);

        // then deserialize back
        WrappedList output = mapper.readValue(json, WrappedList.class);
        assertNotNull(output);
        assertNotNull(output.value);
        assertEquals(2, output.value.length);
    }
    
    public void testUnwrappedLists() throws Exception
    {
        XmlMapper mapper = new XmlMapper();

        UnwrappedList list = new UnwrappedList();
        list.value = new Value[] { new Value("c"), new Value("d") };
        String json = mapper.writeValueAsString(list);
        
//        System.out.println("Unwrapped == "+json);
//        withJAXB(list);
        assertEquals("<list><value><v>c</v></value><value><v>d</v></value></list>", json);

        // then deserialize back
        UnwrappedList output = mapper.readValue(json, UnwrappedList.class);
        assertNotNull(output);
        assertNotNull(output.value);
        assertEquals(2, output.value.length);
    
    }

    /**
     * Test to verify that default wrapping setting is used
     */
    public void testDefaultWrapping() throws Exception
    {
        // by default, should be using wrapping, so:
        XmlMapper mapper = new XmlMapper();
        DefaultList input = new DefaultList();
        input.value = new Value[] { new Value("a"), new Value("b") };
        String json = mapper.writeValueAsString(input);
        assertEquals("<DefaultList><value><value><v>a</v></value><value><v>b</v></value></value></DefaultList>", json);
        DefaultList output = mapper.readValue(json, DefaultList.class);
        assertNotNull(output.value);
        assertEquals(2, output.value.length);

        // but can be changed not to use wrapping by default
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        mapper = new XmlMapper(module);
        json = mapper.writeValueAsString(input);
        assertEquals("<DefaultList><value><v>a</v></value><value><v>b</v></value></DefaultList>", json);
        output = mapper.readValue(json, DefaultList.class);
        assertNotNull(output.value);
        assertEquals(2, output.value.length);
    }

    public void testDefaultWrappingWithEmptyLists() throws Exception
    {
        // by default, should be using wrapping, so:
        XmlMapper mapper = new XmlMapper();
        String json = "<DefaultList><value><value></value></value></DefaultList>";
        DefaultList output = mapper.readValue(json, DefaultList.class);
        assertNotNull(output.value);
        assertEquals(1, output.value.length);

        // but without, should work as well
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        mapper = new XmlMapper(module);
        json = "<DefaultList><value></value></DefaultList>";
        output = mapper.readValue(json, DefaultList.class);
        assertNotNull(output.value);
        assertEquals(1, output.value.length);
    }

    // // [Issue#64]
    public void testOptionalsWithMissingType() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
//        Optionals ob = MAPPER.readValue("<MultiOptional><optional type='work'>123-456-7890</optional></MultiOptional>",
        Optionals ob = mapper.readValue("<MultiOptional><optional>123-456-7890</optional></MultiOptional>",
                Optionals.class);
        assertNotNull(ob);
        assertNotNull(ob.optional);
        assertEquals(1, ob.optional.size());

//            System.err.println("ob: " + ob); // works fine

        Optional opt = ob.optional.get(0);
        assertEquals("123-456-7890", opt.number);
        assertEquals("NOT SET", opt.type);
    }

    /*
    void withJAXB(Object ob) throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance(ob.getClass());
        Marshaller m = jc.createMarshaller();
        System.out.print("JAXB      -> ");
        StringWriter sw = new StringWriter();
        m.marshal(ob, sw);
        String xml = sw.toString();
        if (xml.indexOf("<?xml") == 0) {
            xml = xml.substring(xml.indexOf("?>")+2);
        }
        System.out.println(xml);
   }
   */
}
