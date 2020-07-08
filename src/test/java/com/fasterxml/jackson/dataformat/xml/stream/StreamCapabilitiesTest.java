package com.fasterxml.jackson.dataformat.xml.stream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadCapability;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class StreamCapabilitiesTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testReadCapabilities() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<root />")) {
            assertTrue(p.getReadCapabilities().isEnabled(StreamReadCapability.DUPLICATE_PROPERTIES));
            assertTrue(p.getReadCapabilities().isEnabled(StreamReadCapability.SCALARS_AS_OBJECTS));
            assertTrue(p.getReadCapabilities().isEnabled(StreamReadCapability.UNTYPED_SCALARS));
        }
    }
}
