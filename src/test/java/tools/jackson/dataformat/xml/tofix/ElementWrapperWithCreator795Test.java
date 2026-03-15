package tools.jackson.dataformat.xml.tofix;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;

import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#795] Element wrapper not applied with creator-based deserialization
public class ElementWrapperWithCreator795Test extends XmlTestUtil
{
    static class HttpHeader {
        private String name;
        private String value;

        protected HttpHeader() { }
        public HttpHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String n) { name = n; }
        public String getValue() { return value; }
        public void setValue(String v) { value = v; }
    }

    // Creator-based (immutable) class: wrapper + creator triggers name mismatch
    static class Config {
        @JacksonXmlElementWrapper(localName = "httpHeaders")
        @JacksonXmlProperty(localName = "property")
        final List<HttpHeader> httpHeaders;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Config(
                @JacksonXmlElementWrapper(localName = "httpHeaders")
                @JacksonXmlProperty(localName = "property")
                List<HttpHeader> httpHeaders)
        {
            this.httpHeaders = httpHeaders;
        }

        public List<HttpHeader> getHttpHeaders() { return httpHeaders; }
    }

    private static final String XML =
            "<Config>"
            + "<httpHeaders>"
            + "<property>"
            + "<name>X-JFrog-Art-Api</name>"
            + "<value>myApiToken</value>"
            + "</property>"
            + "</httpHeaders>"
            + "</Config>";

    @Test
    public void testSerializeWithWrapper() throws Exception
    {
        Config config = new Config(List.of(new HttpHeader("X-JFrog-Art-Api", "myApiToken")));
        String xml = newMapper().writeValueAsString(config);
        assertEquals(XML, xml);
    }

    // Deserialization fails: XmlBeanDeserializerModifier renames property to wrapper name
    // ("httpHeaders") but creator property retains original name ("property"), causing mismatch.
    @JacksonTestFailureExpected
    @Test
    public void testDeserializeWithWrapper() throws Exception
    {
        Config result = newMapper().readValue(XML, Config.class);
        assertNotNull(result.getHttpHeaders());
        assertEquals(1, result.getHttpHeaders().size());
        assertEquals("X-JFrog-Art-Api", result.getHttpHeaders().get(0).getName());
        assertEquals("myApiToken", result.getHttpHeaders().get(0).getValue());
    }
}
