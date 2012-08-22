package com.fasterxml.jackson.dataformat.xml.failing;

import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class TestUnwrappedLists extends XmlTestBase
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
//        @XmlElementWrapper(name="")
        @JacksonXmlElementWrapper(localName = "")
        public Value[] value;
    }
    
    /*
    /**********************************************************************
    /* Unit tests
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
        
        System.out.println("Unwrapped == "+json);
//        withJAXB(list);
        assertEquals("<list><value><v>c</v></value><value><v>d</v></value></list>", json);

        // then deserialize back
        UnwrappedList output = mapper.readValue(json, UnwrappedList.class);
        assertNotNull(output);
        assertNotNull(output.value);
        assertEquals(2, output.value.length);
    
    }

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
}
