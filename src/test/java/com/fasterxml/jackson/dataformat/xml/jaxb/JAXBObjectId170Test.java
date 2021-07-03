package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class JAXBObjectId170Test extends XmlTestBase
{
    static class Company
    {
        @XmlElementWrapper(name = "computers")
        @XmlElement(name = "computer")
        public List<Computer> computers = new ArrayList<Computer>();

        @XmlElementWrapper(name = "employees")
        @XmlElement(name = "employee")
        public List<Employee> employees = new ArrayList<Employee>();

        public Company() { }

        public Company add(Computer computer) {
            if (computers == null) {
                computers = new ArrayList<Computer>();
            }
            computers.add(computer);
            return this;
        }
    }

    @XmlType(name = "employee")
    @XmlAccessorType(XmlAccessType.FIELD)
    static class Employee {
      @XmlAttribute
      @XmlID
      public String id;

      @XmlAttribute
      public String name;

      @XmlIDREF
      public Computer computer;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    static class DesktopComputer extends Computer {
        public String location;

        protected DesktopComputer() { }
        public DesktopComputer with(String id0, String l) {
            id = id0;
            location = l;
            return this;
        }
    }

    static class LaptopComputer extends Computer {
        public String vendor;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // for [dataformat-xml#178]
    public void testPolyIdList178() throws Exception
    {
        final String XML =
"<company>\n"+
"<computers>\n"+
"    <computers>\n"+
"      <desktop id='computer-1'>\n"+
"        <location>Bangkok</location>\n"+
"      </desktop>\n"+
"    </computers>\n"+
"    <computers>\n"+
"      <desktop id='computer-2'>\n"+
"        <location>Pattaya</location>\n"+
"      </desktop>\n"+
"    </computers>\n"+
"    <computers>\n"+
"      <laptop id='computer-3'>\n"+
"        <vendor>Apple</vendor>\n"+
"      </laptop>\n"+
"    </computers>\n"+
"  </computers>\n"+
"  <employees>\n"+
"    <employee id='emp-1' name='Robert Patrick'>\n"+
"      <computer>computer-3</computer>\n"+
"    </employee>\n"+
"    <employee id='emp-2' name='Michael Smith'>\n"+
"      <computer>computer-2</computer>\n"+
"    </employee>\n"+
"  </employees>\n"+
"</company>\n"
                ;

        AnnotationIntrospector xmlIntr = jakartaXMLBindAnnotationIntrospector();
        AnnotationIntrospector intr = XmlAnnotationIntrospector.Pair.instance
                (xmlIntr, new JacksonAnnotationIntrospector());
        XmlMapper mapper = mapperBuilder()
               .defaultUseWrapper(false)
               .annotationIntrospector(intr)
               .build();

        // should be default but doesn't seem to be?
        mapper.setAnnotationIntrospector(intr);

        Company result = mapper.readValue(XML, Company.class);
        assertNotNull(result);
        assertNotNull(result.employees);
        assertEquals(2, result.employees.size());
        Employee empl2 = result.employees.get(1);
        Computer comp2 = empl2.computer;
        assertEquals(DesktopComputer.class, comp2.getClass());
        assertEquals("Pattaya", ((DesktopComputer) comp2).location);
    }
}
