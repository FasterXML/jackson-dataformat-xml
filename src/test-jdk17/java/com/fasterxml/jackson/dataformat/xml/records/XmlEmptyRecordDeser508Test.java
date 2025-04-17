package com.fasterxml.jackson.dataformat.xml.records;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#508]
public class XmlEmptyRecordDeser508Test extends XmlTestUtil
{
    static class EmptyClass508 {
    }

    public record EmptyRecord508() {
    }

    private final XmlMapper MAPPER = new XmlMapper();

    @Test
    public void testEmptyPOJO() throws Exception {
        assertNotNull(MAPPER.readValue("<EmptyClass508/>", EmptyClass508.class));
    }

    @Test
    public void testEmptyRecord() throws Exception {
        assertNotNull(MAPPER.readValue("<EmptyRecord508/>", EmptyRecord508.class));
    }
}
