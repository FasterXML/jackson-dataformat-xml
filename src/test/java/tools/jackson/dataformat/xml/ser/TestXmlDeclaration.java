package tools.jackson.dataformat.xml.ser;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.XmlWriteFeature;

public class TestXmlDeclaration extends XmlTestBase
{
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    public void testXml10Declaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("123"));
        assertEquals(xml, "<?xml version='1.0' encoding='UTF-8'?><StringBean><text>123</text></StringBean>");
    }

    public void testXml11Declaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .enable(XmlWriteFeature.WRITE_XML_1_1)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("abcd"));
        assertEquals(xml, "<?xml version='1.1' encoding='UTF-8'?><StringBean><text>abcd</text></StringBean>");
    }
}
