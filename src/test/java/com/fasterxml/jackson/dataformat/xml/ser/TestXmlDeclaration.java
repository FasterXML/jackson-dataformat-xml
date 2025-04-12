package com.fasterxml.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestXmlDeclaration extends XmlTestUtil
{
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    @Test
    public void testXml10Declaration() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        String xml = mapper.writeValueAsString(new StringBean("123"));
        assertEquals(xml, "<?xml version='1.0' encoding='UTF-8'?><StringBean><text>123</text></StringBean>");
    }

    @Test
    public void testXml11Declaration() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        String xml = mapper.writeValueAsString(new StringBean("abcd"));
        assertEquals(xml, "<?xml version='1.1' encoding='UTF-8'?><StringBean><text>abcd</text></StringBean>");
    }

    @Test
    public void testXml11DeclarationWithStandalone() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_STANDALONE_YES_TO_XML_DECLARATION, true);
        String xml = mapper.writeValueAsString(new StringBean("abcd"));
        assertEquals("<?xml version='1.1' encoding='UTF-8' standalone='yes'?><StringBean><text>abcd</text></StringBean>", xml);
    }

    @Test
    public void testXml10DeclarationWithStandalone() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_STANDALONE_YES_TO_XML_DECLARATION, true);
        String xml = mapper.writeValueAsString(new StringBean("abcd"));
        assertEquals("<?xml version='1.0' encoding='UTF-8' standalone='yes'?><StringBean><text>abcd</text></StringBean>", xml);
    }
    
}
