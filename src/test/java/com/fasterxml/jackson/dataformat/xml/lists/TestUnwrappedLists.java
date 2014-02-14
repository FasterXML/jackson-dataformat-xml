package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

// Failing unit test(s) wrt [Issue#64]
public class TestUnwrappedLists extends XmlTestBase
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
}