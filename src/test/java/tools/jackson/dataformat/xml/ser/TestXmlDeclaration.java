package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

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
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("123"));
        assertEquals(xml, "<?xml version='1.0' encoding='UTF-8'?><StringBean><text>123</text></StringBean>");
    }

    @Test
    public void testXml11Declaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .enable(XmlWriteFeature.WRITE_XML_1_1)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("abcd"));
        assertEquals(xml, "<?xml version='1.1' encoding='UTF-8'?><StringBean><text>abcd</text></StringBean>");
    }
}
