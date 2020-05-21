package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

// [dataformat-xml#314]
public class ListDeser314Test extends XmlTestBase
{
    static class Customer314 {
        @JacksonXmlElementWrapper(localName = "Customer", useWrapping = false)
        @JacksonXmlProperty(localName = "Address")
        public List<Address314> address;
    }

    static class Address314 {
        public String stateProv;
        public CountryName314 countryName;
    }

    static class CountryName314 {
        public String code;
        @JacksonXmlText
        public String name;
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = mapperBuilder()
            .propertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
            .build();

    // [dataform#314]
    public void testDeser314Order1() throws Exception
    {
        String content = ""
                + "<Customer>\n"
                + "  <Address>\n"
                + "    <StateProv StateCode='DE-NI'>Niedersachsen</StateProv>\n"
                + "    <CountryName Code='DE'>Deutschland</CountryName>\n"
                + "  </Address>\n"
                + "</Customer>"
                ;
        Customer314 result = MAPPER.readValue(content, Customer314.class);
        assertNotNull(result);
    }

    public void testDeser314Order2() throws Exception
    {
        String content = ""
                + "<Customer>\n"
                + "  <Address>\n"
                + "    <CountryName Code='DE'>Deutschland</CountryName>\n"
                + "    <StateProv StateCode='DE-NI'>Niedersachsen</StateProv>\n"
                + "  </Address>\n"
                + "</Customer>"
                ;
        Customer314 result = MAPPER.readValue(content, Customer314.class);
        assertNotNull(result);
    }

    public void testDeser314Address() throws Exception
    {
        String content = ""
                + "  <Address>\n"
                + "    <CountryName Code=\"DE\">Deutschland</CountryName>\n"
                + "    <StateProv StateCode=\"DE-NI\">Niedersachsen</StateProv>\n"
                + "  </Address>\n"
                ;
        Address314 result = MAPPER.readValue(content, Address314.class);
        assertNotNull(result);
    }
}
