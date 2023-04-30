package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import java.io.IOException;

public class TestCharset extends XmlTestBase
{
    static class StringBean {
        public String 象形字;
    }

    public void testBig5() throws IOException
    {
        StringBean stringBean = new StringBean();
        stringBean.象形字 = "pictogram";
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        byte[] xml = xmlMapper.writeValueAsBytes(stringBean, "Big5");
        String xmlText = new String(xml, "Big5");
        String expected =
                "<?xml version='1.1' encoding='Big5'?><StringBean><象形字>pictogram</象形字></StringBean>";
        assertEquals(expected, xmlText);
    }
}
