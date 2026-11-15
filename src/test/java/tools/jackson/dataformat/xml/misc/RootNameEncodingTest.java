package tools.jackson.dataformat.xml.misc;

import java.util.*;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Root element name (class name, `@JsonRootName`, `ObjectWriter.withRootName()`)
// must go through the configured `XmlNameProcessor` like property and (since #918)
// collection wrapper names: with the AlwaysOn base64 processor the reader decodes
// every element name, so an unencoded root name like "Point" (length % 4 == 1)
// would fail to decode.
public class RootNameEncodingTest extends XmlTestUtil
{
    public static class Point {
        public List<String> values = new ArrayList<>();
    }

    @Test
    public void testAlwaysOnBase64RootNameRoundTrip() throws Exception {
        Point p = new Point();
        p.values.add("a");

        XmlMapper mapper = XmlMapper.builder(
                XmlFactory.builder()
                    .xmlNameProcessor(XmlNameProcessors.newAlwaysOnBase64Processor())
                    .build()
        ).build();

        final String res = mapper.writeValueAsString(p);
        Point result = mapper.readValue(res, Point.class);
        assertEquals(p.values, result.values);
    }
}
