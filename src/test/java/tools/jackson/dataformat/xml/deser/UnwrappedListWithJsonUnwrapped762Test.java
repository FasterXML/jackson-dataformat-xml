package tools.jackson.dataformat.xml.deser;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for [dataformat-xml#762]: unwrapped lists inside {@code @JsonUnwrapped}
 * properties should deserialize correctly.
 */
public class UnwrappedListWithJsonUnwrapped762Test extends XmlTestUtil
{
    // [dataformat-xml#762] - original issue reproducer with List<String>
    static class Pojo762 {
        private Nested762 nested;

        @JsonUnwrapped
        public Nested762 getNested() { return nested; }
        public void setNested(Nested762 nested) { this.nested = nested; }
    }

    static class Nested762 {
        private List<String> values;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "value")
        public List<String> getValues() { return values; }
        public void setValues(List<String> values) { this.values = values; }
    }

    // [dataformat-xml#299] - related: unwrapped list of objects
    static class Request {
        @JsonUnwrapped
        public Composite composite = new Composite();
    }

    static class Composite {
        public String messageId;
        public Integer number;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Header> headers = new ArrayList<>();
    }

    static class Header {
        public String headerId;
    }

    private final ObjectMapper MAPPER = newMapper();

    @Test
    public void testUnwrappedStringList762() throws Exception {
        final String xml = "<pojo>\n"
                + "  <value>a</value>\n"
                + "  <value>b</value>\n"
                + "</pojo>";
        Pojo762 result = MAPPER.readValue(xml, Pojo762.class);
        assertNotNull(result.getNested());
        assertNotNull(result.getNested().getValues());
        assertEquals(2, result.getNested().getValues().size());
        assertEquals("a", result.getNested().getValues().get(0));
        assertEquals("b", result.getNested().getValues().get(1));
    }

    @Test
    public void testUnwrappedObjectList299() throws Exception {
        final Request request = new Request();
        request.composite.messageId = "ABC";
        request.composite.number = 123;

        final Header header1 = new Header();
        header1.headerId = "headerID1";
        final Header header2 = new Header();
        header2.headerId = "headerID2";

        request.composite.headers.add(header1);
        request.composite.headers.add(header2);

        String xmlString = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(request);

        final Request anotherRequest = MAPPER.readValue(xmlString, Request.class);

        assertEquals("ABC", anotherRequest.composite.messageId);
        assertEquals(Integer.valueOf(123), anotherRequest.composite.number);
        assertEquals(2, anotherRequest.composite.headers.size());
        assertEquals("headerID1", anotherRequest.composite.headers.get(0).headerId);
        assertEquals("headerID2", anotherRequest.composite.headers.get(1).headerId);
    }
}
