package tools.jackson.dataformat.xml.stream;

import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadCapability;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

public class StreamCapabilitiesTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testReadCapabilities() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<root />")) {
            assertTrue(p.streamReadCapabilities().isEnabled(StreamReadCapability.DUPLICATE_PROPERTIES));
            assertTrue(p.streamReadCapabilities().isEnabled(StreamReadCapability.SCALARS_AS_OBJECTS));
            assertTrue(p.streamReadCapabilities().isEnabled(StreamReadCapability.UNTYPED_SCALARS));
        }
    }
}
