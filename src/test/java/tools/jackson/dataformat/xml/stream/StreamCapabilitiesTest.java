package tools.jackson.dataformat.xml.stream;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadCapability;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamCapabilitiesTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testReadCapabilities() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<root />")) {
            assertTrue(p.streamReadCapabilities().isEnabled(StreamReadCapability.DUPLICATE_PROPERTIES));
            assertTrue(p.streamReadCapabilities().isEnabled(StreamReadCapability.SCALARS_AS_OBJECTS));
            assertTrue(p.streamReadCapabilities().isEnabled(StreamReadCapability.UNTYPED_SCALARS));
        }
    }
}
