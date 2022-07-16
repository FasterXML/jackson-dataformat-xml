package tools.jackson.dataformat.xml.ser;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

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
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("123"));
        assertEquals(xml, "<?xml version='1.0' encoding='UTF-8'?><StringBean><text>123</text></StringBean>");
    }

    public void testXml11Declaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .enable(ToXmlGenerator.Feature.WRITE_XML_1_1)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("abcd"));
        assertEquals(xml, "<?xml version='1.1' encoding='UTF-8'?><StringBean><text>abcd</text></StringBean>");
    }
}
