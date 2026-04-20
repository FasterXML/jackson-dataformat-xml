package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class XmlGeneratorInitializerTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#150]: DTD writing
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
