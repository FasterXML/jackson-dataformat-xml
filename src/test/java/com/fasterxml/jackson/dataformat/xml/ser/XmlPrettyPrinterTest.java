package com.fasterxml.jackson.dataformat.xml.ser;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class XmlPrettyPrinterTest extends XmlTestBase
{
    static class StringWrapperBean {
        public StringWrapper string;
        
        public StringWrapperBean() { }
        public StringWrapperBean(String s) { string = new StringWrapper(s); }
    }

    static class IntWrapperBean {
        public IntWrapper wrapped;
        
        public IntWrapperBean() { }
        public IntWrapperBean(int i) { wrapped = new IntWrapper(i); }
    }

    // [dataformat-xml#45]
    static class AttrBean {
        @JacksonXmlProperty(isAttribute=true)
        public int count = 3;
    }

    static class AttrBean2 {
        @JacksonXmlProperty(isAttribute=true)
        public int count = 3;

        public int value = 14;
    }

    public class PojoFor123
    {
        @JacksonXmlProperty(isAttribute = true)
        public String name;

        @JsonInclude(JsonInclude.Include.NON_EMPTY) 
        public String property;
        
        public PojoFor123(String name) {
            this.name = name;       
        }
    }

    // for [dataformat-xml#172]
    static class Company {
        @JacksonXmlElementWrapper(localName="e")
        public List<Employee> employee = new ArrayList<Employee>();
    }

    @JsonPropertyOrder({"id", "type"})
    static class Employee {
        public String id;
        public EmployeeType type;

        public Employee(String id) {
            this.id = id;
            type = EmployeeType.FULL_TIME;
        }
    }

    static enum EmployeeType {
        FULL_TIME;
    }

    /*
    /**********************************************************
    /* Set up
    /**********************************************************
     */

    protected XmlMapper _xmlMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _xmlMapper = new XmlMapper();
        _xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // Verify [dataformat-xml#1]
    public void testSimpleStringBean() throws Exception
    {
        StringWrapperBean input = new StringWrapperBean("abc");
        String xml = _xmlMapper.writeValueAsString(input); 

        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        // Let's verify we get similar stuff back, first:
        StringWrapperBean result = _xmlMapper.readValue(xml, StringWrapperBean.class);
        assertNotNull(result);
        assertEquals("abc", result.string.str);

        // Try via ObjectWriter as well
        xml = _xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        result = _xmlMapper.readValue(xml, StringWrapperBean.class);
        assertNotNull(result);
        assertEquals("abc", result.string.str);
    }

    public void testSimpleIntBean() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new IntWrapperBean(42)); 
        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
        	fail("No indentation: XML == "+xml);
        }
        // Let's verify we get similar stuff back, first:
        IntWrapperBean result = _xmlMapper.readValue(xml, IntWrapperBean.class);
        assertNotNull(result);
        assertEquals(42, result.wrapped.i);
    }
    
    public void testSimpleMap() throws Exception
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        String xml = _xmlMapper.writeValueAsString(map);

        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        
        // Let's verify we get similar stuff back, first:
        Map<?,?> result = _xmlMapper.readValue(xml, Map.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("b", map.get("a"));
    }

    // [dataformat-xml#45]: Use of attributes should not force linefeed for empty elements
    public void testWithAttr() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new AttrBean());
        assertEquals("<AttrBean count=\"3\"/>\n", xml);
        String xml2 = _xmlMapper.writeValueAsString(new AttrBean2());
        assertEquals("<AttrBean2 count=\"3\">\n  <value>14</value>\n</AttrBean2>\n", xml2);
    }

    public void testEmptyElem() throws Exception
    {
        PojoFor123 simple = new PojoFor123("foobar");
        String xml = _xmlMapper.writeValueAsString(simple);
        assertEquals("<PojoFor123 name=\"foobar\"/>\n", xml);
    }

    public void testMultiLevel172() throws Exception
    {
        Company root = new Company();
        root.employee.add(new Employee("abc"));
        String xml = _xmlMapper.writer()
                .with(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .writeValueAsString(root);
        // unify possible apostrophes to quotes
        xml = a2q(xml);
        // with indentation, should get linefeeds in prolog/epilog too
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                +"<Company>\n"
                +"  <e>\n"
                +"    <employee>\n"
                +"      <id>abc</id>\n"
                +"      <type>FULL_TIME</type>\n"
                +"    </employee>\n"
                +"  </e>\n"
                +"</Company>\n",
                xml);
    }
}
