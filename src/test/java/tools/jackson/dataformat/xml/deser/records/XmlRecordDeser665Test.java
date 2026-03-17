package tools.jackson.dataformat.xml.deser.records;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#665]: @JacksonXmlProperty without localName on records
//   should not cause "Duplicate creator property" error
public class XmlRecordDeser665Test extends XmlTestUtil
{
    // Bare @JacksonXmlProperty (no arguments) on a record — was failing
    record SimpleRecord(@JacksonXmlProperty String name, String value) {}

    // @JacksonXmlProperty(isAttribute=true) without localName — the main use case
    record AttributeRecord(@JacksonXmlProperty(isAttribute = true) String id, String name) {}

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testBareAnnotationOnRecord() throws Exception {
        String xml = "<SimpleRecord><name>test</name><value>val</value></SimpleRecord>";
        SimpleRecord result = MAPPER.readValue(xml, SimpleRecord.class);
        assertEquals("test", result.name());
        assertEquals("val", result.value());
    }

    @Test
    public void testIsAttributeWithoutLocalNameOnRecord() throws Exception {
        String xml = "<AttributeRecord id='123'><name>test</name></AttributeRecord>";
        AttributeRecord result = MAPPER.readValue(xml, AttributeRecord.class);
        assertEquals("123", result.id());
        assertEquals("test", result.name());
    }

    // Verify serialization round-trip works too
    @Test
    public void testSerializationRoundTrip() throws Exception {
        AttributeRecord original = new AttributeRecord("123", "test");
        String xml = MAPPER.writeValueAsString(original);
        AttributeRecord result = MAPPER.readValue(xml, AttributeRecord.class);
        assertEquals(original.id(), result.id());
        assertEquals(original.name(), result.name());
    }
}
