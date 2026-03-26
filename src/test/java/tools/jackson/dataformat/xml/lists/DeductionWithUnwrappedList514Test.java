package tools.jackson.dataformat.xml.lists;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.*;
import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#514]: Deserialisation to polymorphic class using
// `JsonTypeInfo.Id.DEDUCTION` and `@JacksonXmlElementWrapper` fails
public class DeductionWithUnwrappedList514Test extends XmlTestUtil
{
    static class NestedClass {
        @JacksonXmlProperty(localName = "Text")
        public String text;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
        @JsonSubTypes.Type(Success514.class),
        @JsonSubTypes.Type(Error514.class)
    })
    static abstract class Response514 { }

    static class Success514 extends Response514 {
        @JacksonXmlProperty(localName = "SomeData")
        public String someData;

        @JacksonXmlProperty(localName = "Detail")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<NestedClass> details;
    }

    static class Error514 extends Response514 {
        @JacksonXmlProperty(localName = "ErrorCode")
        public String errorCode;
    }

    @JsonRootName("RootTag")
    static class WholeResponse {
        @JacksonXmlProperty(localName = "Response")
        public Response514 response;
    }

    // Direct deserialization without deduction — should work
    @JsonRootName("RootTag")
    static class WholeResponseDirect {
        @JacksonXmlProperty(localName = "Response")
        public Success514 response;
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testDirectDeserializationWithoutDeduction() throws Exception
    {
        String xml =
                "<RootTag>"
                + "<Response>"
                + "<SomeData>hello</SomeData>"
                + "<Detail><Text>hello world</Text></Detail>"
                + "</Response>"
                + "</RootTag>";
        WholeResponseDirect result = MAPPER.readValue(xml, WholeResponseDirect.class);
        assertNotNull(result);
        assertNotNull(result.response);
        assertEquals("hello", result.response.someData);
        assertNotNull(result.response.details);
        assertEquals(1, result.response.details.size());
        assertEquals("hello world", result.response.details.get(0).text);
    }

    // [dataformat-xml#514]
    @Test
    public void testDeductionWithUnwrappedList() throws Exception
    {
        String xml =
                "<RootTag>"
                + "<Response>"
                + "<SomeData>hello</SomeData>"
                + "<Detail><Text>hello world</Text></Detail>"
                + "</Response>"
                + "</RootTag>";
        WholeResponse result = MAPPER.readValue(xml, WholeResponse.class);
        assertNotNull(result);
        assertNotNull(result.response);
        assertInstanceOf(Success514.class, result.response);
        Success514 success = (Success514) result.response;
        assertEquals("hello", success.someData);
        assertNotNull(success.details);
        assertEquals(1, success.details.size());
        assertEquals("hello world", success.details.get(0).text);
    }
}
