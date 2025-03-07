package tools.jackson.dataformat.xml;

import org.junit.jupiter.api.Test;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.dataformat.xml.deser.FromXmlParser;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class XmlMapperTest extends XmlTestUtil
{

    @Test
    public void testBuilderWithJackson2Defaults() throws Exception
    {
        XmlMapper mapper = XmlMapper.builderWithJackson2Defaults().build();
        assertFalse(mapper.isEnabled(StreamReadFeature.USE_FAST_DOUBLE_PARSER));
        assertFalse(mapper.isEnabled(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER));

        XMLOutputFactory outputFactory = mapper.tokenStreamFactory().getXMLOutputFactory();
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ToXmlGenerator gen =
                     mapper.createGenerator(
                             outputFactory.createXMLStreamWriter(bos))
        ) {
            assertFalse(gen.isEnabled(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL));
            assertFalse(gen.isEnabled(XmlWriteFeature.UNWRAP_ROOT_OBJECT_NODE));
            assertFalse(gen.isEnabled(XmlWriteFeature.AUTO_DETECT_XSI_TYPE));
            assertFalse(gen.isEnabled(XmlWriteFeature.WRITE_XML_SCHEMA_CONFORMING_FLOATS));
            // need to write something to the generator to avoid exception
            final Point p = new Point(1, 2);
            mapper.writeValue(gen, p);
        }

        final byte[] xml = "<root/>".getBytes(StandardCharsets.UTF_8);
        XMLInputFactory inputFactory = mapper.tokenStreamFactory().getXMLInputFactory();
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(xml);
                FromXmlParser parser =
                        mapper.createParser(
                                inputFactory.createXMLStreamReader(bis))
        ) {
            assertFalse(parser.isEnabled(XmlReadFeature.AUTO_DETECT_XSI_TYPE));
        }
    }
}
