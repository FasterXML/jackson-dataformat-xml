package com.fasterxml.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for [dataformat-xml#114]: Support for STRICT_DUPLICATE_DETECTION
 */
public class StrictDuplicateDetection114Test extends XmlTestUtil
{
    static class TestBean {
        public String field1;
        public String field2;
    }

    private final XmlMapper XML_MAPPER = XmlMapper.builder()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .build();

    private final ObjectMapper JSON_MAPPER = JsonMapper.builder()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .build();

    // [dataformat-xml#114]
    @Test
    public void testStrictDuplicateDetectionWithPOJO() throws Exception
    {
        // First verify JSON mapper properly rejects duplicates
        final String jsonWithDup = "{\"field1\":\"value1\",\"field1\":\"value2\"}";

        StreamReadException e = assertThrows(StreamReadException.class, () -> {
            JSON_MAPPER.readValue(jsonWithDup, TestBean.class);
        });
        assertTrue(e.getMessage().contains("Duplicate field"),
                "Expected 'Duplicate field' error, got: " + e.getMessage());

        // Now test XML mapper should also reject duplicates
        final String xmlWithDup = "<TestBean><field1>value1</field1><field1>value2</field1></TestBean>";

        e = assertThrows(StreamReadException.class, () -> {
            XML_MAPPER.readValue(xmlWithDup, TestBean.class);
        });
        assertTrue(e.getMessage().contains("Duplicate field"),
                "Expected 'Duplicate field' error, got: " + e.getMessage());
    }

    @Test
    public void testNoDuplicatesShouldWork() throws Exception
    {
        final String xml = "<TestBean><field1>value1</field1><field2>value2</field2></TestBean>";

        TestBean bean = XML_MAPPER.readValue(xml, TestBean.class);
        assertNotNull(bean);
        assertEquals("value1", bean.field1);
        assertEquals("value2", bean.field2);
    }

    @Test
    public void testDuplicateDetectionDisabledByDefault() throws Exception
    {
        XmlMapper mapper = newMapper(); // default mapper without strict duplicate detection

        // Should allow duplicates by default (last value wins)
        final String xmlWithDup = "<TestBean><field1>value1</field1><field1>value2</field1></TestBean>";

        TestBean bean = mapper.readValue(xmlWithDup, TestBean.class);
        assertNotNull(bean);
        assertEquals("value2", bean.field1); // last value wins
    }
}
