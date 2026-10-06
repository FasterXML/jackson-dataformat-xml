package tools.jackson.dataformat.xml.tofix;

import java.util.*;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Root element name (class name, `@JsonRootName`, `ObjectWriter.withRootName()`)
// is written without going through the configured `XmlNameProcessor`, unlike
// property and (since #918) collection wrapper names. With the AlwaysOn base64
// processor the reader decodes every element name, so the round trip fails for
// root names that are not valid base64url (e.g. length % 4 == 1).
public class RootNameEncodingTest extends XmlTestUtil
{
    public static class Point {
        public List<String> values = new ArrayList<>();
    }

    @JacksonTestFailureExpected
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
