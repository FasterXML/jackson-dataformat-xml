package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import java.io.IOException;
import java.nio.charset.Charset;

public class TestCharset extends XmlTestBase
{
    static class StringBean {
        public String 象形字;
    }

    public void testBig5Bytes() throws IOException
    {
        Charset big5 = Charset.forName("Big5");
        StringBean stringBean = new StringBean();
        stringBean.象形字 = "pictogram";
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        byte[] xml = xmlMapper.writeValueAsBytes(stringBean, big5);
        String xmlText = new String(xml, big5);
        String expected =
                "<?xml version='1.1' encoding='Big5'?><StringBean><象形字>pictogram</象形字></StringBean>";
        assertEquals(expected, xmlText);
    }

    public void testBig5RoundTrip() throws IOException
    {
        Charset big5 = Charset.forName("Big5");
        StringBean stringBean = new StringBean();
        stringBean.象形字 = "pictogram";
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        byte[] xml = xmlMapper.writeValueAsBytes(stringBean, big5);
        StringBean stringBean1 = xmlMapper.readValue(xml, StringBean.class);
        assertEquals(stringBean.象形字, stringBean1.象形字);
    }

    public void testBig5ObjectWriter() throws IOException
    {
        Charset big5 = Charset.forName("Big5");
        StringBean stringBean = new StringBean();
        stringBean.象形字 = "pictogram";
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        byte[] xml = xmlMapper.writer().writeValueAsBytes(stringBean);
        System.out.write(xml);
        System.out.println();
    }
}
