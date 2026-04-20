package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class XmlGeneratorInitializerTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#150]: DTD writing -- ok cases
    @Test
    public void testDTDWithOnlyRootElement() throws Exception
    {
        ObjectWriter w = MAPPER.writer().with(
                new XmlGeneratorInitializer()
                        .setDTD("StringBean", null, null, null));
        assertEquals(a2q("<!DOCTYPE StringBean>"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    @Test
    public void testDTDWithPublicId() throws Exception
    {
        ObjectWriter w = MAPPER.writer().with(
                new XmlGeneratorInitializer()
                        .setDTD("StringBean", "system", "http://foo.bar", ""));
        assertEquals(a2q("<!DOCTYPE StringBean PUBLIC 'http://foo.bar' 'system'>"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    @Test
    public void testDTDWithSystemIdOnly() throws Exception
    {
        ObjectWriter w = MAPPER.writer().with(
                new XmlGeneratorInitializer()
                        .setDTD("StringBean", "system", "", null));
        assertEquals(a2q("<!DOCTYPE StringBean SYSTEM 'system'>"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    @Test
    public void testDTDWithInternalSubset() throws Exception
    {
        ObjectWriter w = MAPPER.writer().with(
                new XmlGeneratorInitializer()
                        .setDTD("StringBean", "system", "http://foo.bar", "<!ELEMENT root (#PCDATA)>"));
        assertEquals(a2q("<!DOCTYPE StringBean PUBLIC 'http://foo.bar' 'system' "
                +"[<!ELEMENT root (#PCDATA)>]>"
                +"<StringBean><text>test</text></StringBean>"),
                w.writeValueAsString(new StringBean("test")));
    }

    // Verify prolog ordering: XML declaration must come before DOCTYPE
    @Test
    public void testDTDWithXmlDeclaration() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
        ObjectWriter w = mapper.writer().with(
                new XmlGeneratorInitializer()
                        .setDTD("StringBean", "system", "http://foo.bar", null));
        // XML declaration is emitted with single quotes, DOCTYPE with double quotes,
        // so cannot use a2q() on the whole string here.
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>"
                +"<!DOCTYPE StringBean PUBLIC \"http://foo.bar\" \"system\">"
                +"<StringBean><text>test</text></StringBean>",
                w.writeValueAsString(new StringBean("test")));
    }

    // [dataformat-xml#150]: DTD writing -- failing cases
    @Test
    public void testDTDInvalidNoRoot() throws Exception
    {
        try {
            /*ObjectWriter w =*/ MAPPER.writer().with(
                new XmlGeneratorInitializer()
                    .setDTD("", null, null, null));
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Illegal argument for 'rootName': must be");
        }
    }

    // Other tests
}
