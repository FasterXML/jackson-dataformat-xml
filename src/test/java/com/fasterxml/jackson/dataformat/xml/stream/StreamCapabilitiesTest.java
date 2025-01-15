package com.fasterxml.jackson.dataformat.xml.stream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamCapabilitiesTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testReadCapabilities() throws Exception
    {
        try (JsonParser p = MAPPER.createParser("<root />")) {
            assertTrue(p.getReadCapabilities().isEnabled(StreamReadCapability.DUPLICATE_PROPERTIES));
            assertTrue(p.getReadCapabilities().isEnabled(StreamReadCapability.SCALARS_AS_OBJECTS));
            assertTrue(p.getReadCapabilities().isEnabled(StreamReadCapability.UNTYPED_SCALARS));
        }
    }
}
