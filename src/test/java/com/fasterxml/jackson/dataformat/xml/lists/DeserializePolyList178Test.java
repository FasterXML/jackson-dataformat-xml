package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class DeserializePolyList178Test extends XmlTestBase
{
    static class Company {
        public List<Computer> computers;

        public Company() {
            computers = new ArrayList<Computer>();
        }

        public Company add(Computer computer) {
            if (computers == null) {
                computers = new ArrayList<Computer>();
            }
            computers.add(computer);
            return this;
        }
    }

// 02-Jan-2015, tatu: Does not seem to matter; was included in the original reproduction
//    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DesktopComputer.class, name = "desktop"),
            @JsonSubTypes.Type(value = LaptopComputer.class, name = "laptop")
    })
    static class Computer {
        public String id;
    }

    @JsonTypeName("desktop")
    static class DesktopComputer extends Computer {
        public String location;

        protected DesktopComputer() { }
        public DesktopComputer with(String id0, String l) {
            id = id0;
            location = l;
            return this;
        }
    }

    @JsonTypeName("laptop")
    static class LaptopComputer extends Computer {
        public String vendor;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
    // for [dataformat-xml#178]
    public void testPolyIdList178() throws Exception
    {
        Company input = new Company();
        input.add(new DesktopComputer().with("1", "http://foo.com"));
        final String LOC2 = "http://bar.com";
        input.add(new DesktopComputer().with("2", LOC2));
        String xml = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);
//System.out.println("XML:\n"+xml);

        Company result = MAPPER.readValue(xml, Company.class);
        assertNotNull(result.computers);
        assertEquals(2, result.computers.size());
        Computer comp = result.computers.get(1);
        assertNotNull(comp);
        assertEquals(DesktopComputer.class, comp.getClass());
        DesktopComputer dt = (DesktopComputer) comp;
        assertEquals(LOC2, dt.location);
    }
}
