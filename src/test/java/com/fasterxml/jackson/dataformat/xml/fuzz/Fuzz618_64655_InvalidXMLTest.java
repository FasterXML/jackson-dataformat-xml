package com.fasterxml.jackson.dataformat.xml.fuzz;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.core.exc.StreamReadException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.nio.file.*;

public class Fuzz618_64655_InvalidXMLTest extends XmlTestBase
{
    static class AttributeBean
    {
        @JacksonXmlProperty(isAttribute=true, localName="attr")
        public String text = "?";
    }

    static class Optional {
        @JacksonXmlText
        public String number = "NOT SET";
        public String type = "NOT SET";
    }

    // [dataformat-xml#219]
    static class Worker219
    {
        @JacksonXmlProperty(localName = "developer")
        String developer;
        @JacksonXmlProperty(localName = "tester")
        String tester;
        @JacksonXmlProperty(localName = "manager")
        String manager;
    }

    // [dataformat-xml#219]
    @JsonRootName("line")
    static class Line219 {
        public String code; //This should ideally be complex type
        public String amount;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testWithInvalidXml1() throws Exception {
        _testWithInvalidXml(1, "Unexpected end of input");
    }

    public void testWithInvalidXml2() throws Exception {
        _testWithInvalidXml(2, "Unexpected character 'a'");
    }

    public void testWithInvalidXml3() throws Exception {
        _testWithInvalidXml(3, "Unexpected EOF; was expecting a close tag");
    }

    private void _testWithInvalidXml(int ix, String errorToMatch) throws Exception
    {
        try {
            String path = "src/test/java/com/fasterxml/jackson/dataformat/xml/deser/invalid_xml_" + ix;
            MAPPER.readTree(Files.readAllBytes(Paths.get(path)));
        } catch (StreamReadException e) {
            verifyException(e, errorToMatch);
        }
    }
}
