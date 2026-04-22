package tools.jackson.dataformat.xml.ser;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import tools.jackson.databind.cfg.GeneratorInitializer;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Test to ensure that XML module supports
// [https://github.com/FasterXML/jackson-databind/issues/5860] (GeneratorInitializer
// abstraction)
public class GeneralGeneratorInitializerTest extends XmlTestUtil
{
    private final GeneratorInitializer ISO_8859_INITIALIZER = (config, gen) -> {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;
        XMLStreamWriter sw = xmlGen.getStaxWriter();
        try {
            sw.writeStartDocument("ISO-8859-1", "1.0");
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write StartDocument event", e);
        }
    };

    // Test via writeValueAsString (goes through ObjectWriter)
    @Test
    public void testCustomEncodingViaGeneratorInitializer() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .generatorInitializer(ISO_8859_INITIALIZER)
                .build();
        String xml = mapper.writeValueAsString(new StringBean("test"));
        assertTrue(xml.contains("encoding='ISO-8859-1'") || xml.contains("encoding=\"ISO-8859-1\""),
                "Expected ISO-8859-1 encoding in XML declaration, got: " + xml);
    }

    // Test via writeValue(XMLStreamWriter, Object) — XML-specific path
    @Test
    public void testCustomEncodingViaWriteValueXMLStreamWriter() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .generatorInitializer(ISO_8859_INITIALIZER)
                .build();
        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlSW = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        mapper.writeValue(xmlSW, new StringBean("test"));
        xmlSW.close();
        String xml = sw.toString();
        assertTrue(xml.contains("encoding='ISO-8859-1'") || xml.contains("encoding=\"ISO-8859-1\""),
                "Expected ISO-8859-1 encoding in XML declaration, got: " + xml);
    }

    // Test via createGenerator(XMLStreamWriter) — direct generator creation
    @Test
    public void testCustomEncodingViaCreateGeneratorXMLStreamWriter() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .generatorInitializer(ISO_8859_INITIALIZER)
                .build();
        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlSW = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        ToXmlGenerator gen = mapper.createGenerator(xmlSW);
        gen.setNextName(new javax.xml.namespace.QName("root"));
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();
        xmlSW.close();
        String xml = sw.toString();
        assertTrue(xml.contains("encoding='ISO-8859-1'") || xml.contains("encoding=\"ISO-8859-1\""),
                "Expected ISO-8859-1 encoding in XML declaration, got: " + xml);
    }
}
