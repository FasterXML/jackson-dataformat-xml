package tools.jackson.dataformat.xml.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.exc.StreamConstraintsException;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// Verifies that `StreamReadConstraints.maxStringLength` / `maxNameLength` are
// honored when reading XML (element text, attribute values, and element /
// attribute / root names), matching the JSON backend.
public class StringAndNameLengthTest extends XmlTestUtil
{
    private XmlMapper mapperWithStringLimit(int max) {
        return mapperBuilder(XmlFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxStringLength(max).build())
                .build()).build();
    }

    private XmlMapper mapperWithNameLimit(int max) {
        return mapperBuilder(XmlFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNameLength(max).build())
                .build()).build();
    }

    private void drain(XmlMapper mapper, String xml) throws Exception {
        try (JsonParser p = mapper.createParser(xml)) {
            while (p.nextToken() != null) {
                p.getString();
            }
        }
    }

    @Test
    public void testElementTextLength() throws Exception
    {
        final XmlMapper mapper = mapperWithStringLimit(100);
        final String value = "x".repeat(5000);
        try {
            drain(mapper, "<a>" + value + "</a>");
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("String value length (5000) exceeds the maximum allowed"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    @Test
    public void testAttributeValueLength() throws Exception
    {
        final XmlMapper mapper = mapperWithStringLimit(100);
        final String value = "x".repeat(5000);
        try {
            drain(mapper, "<a b='" + value + "'/>");
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("String value length (5000) exceeds the maximum allowed"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    @Test
    public void testElementNameLength() throws Exception
    {
        final XmlMapper mapper = mapperWithNameLimit(50);
        final String name = "n".repeat(5000);
        try {
            drain(mapper, "<a><" + name + ">v</" + name + "></a>");
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("Name length (5000) exceeds the maximum allowed"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    @Test
    public void testAttributeNameLength() throws Exception
    {
        final XmlMapper mapper = mapperWithNameLimit(50);
        final String name = "n".repeat(5000);
        try {
            drain(mapper, "<a " + name + "='v'/>");
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("Name length (5000) exceeds the maximum allowed"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    @Test
    public void testRootElementNameLength() throws Exception
    {
        final XmlMapper mapper = mapperWithNameLimit(50);
        final String name = "n".repeat(5000);
        try {
            drain(mapper, "<" + name + ">v</" + name + ">");
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("Name length (5000) exceeds the maximum allowed"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    // Values within the configured limits must parse unchanged.
    @Test
    public void testWithinLimits() throws Exception
    {
        drain(mapperWithStringLimit(100), "<a b='short'>hello world</a>");
        drain(mapperWithNameLimit(50), "<parent attr='v'><child>text</child></parent>");
    }
}
