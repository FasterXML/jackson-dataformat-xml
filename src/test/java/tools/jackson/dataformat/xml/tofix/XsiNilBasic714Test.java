package tools.jackson.dataformat.xml.tofix;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.*;

public class XsiNilBasic714Test extends XmlTestUtil
{
    private final static String XSI_NS_DECL = "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'";

    // 30-Jan-2025, tatu: To tease out [dataformat-xml#714] let's do this:
    private final XmlMapper MAPPER = mapperBuilder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    // [dataformat-xml#714]: trailing END_OBJECT
    @JacksonTestFailureExpected
    @Test
    public void testRootPojoAsNull() throws Exception
    {
        Point bean = MAPPER.readValue(
"<Point "+XSI_NS_DECL+" xsi:nil='true' />",
                Point.class);
        assertNull(bean);
    }

    // [dataformat-xml#468]: Allow disabling xsi:nil special handling

    // [dataformat-xml#714]: trailing END_OBJECT
    @JacksonTestFailureExpected
    @Test
    public void testDisableXsiNilRootProcessing() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(JsonNode.class);
        final String DOC = "<Point "+XSI_NS_DECL+" xsi:nil='true'></Point>";

        // with default processing:
        assertEquals("null", r.readValue(DOC).toString());

        // 07-Jul-2021, tatu: Alas! 2.x sets format feature flags too late to
        //   affect root element (3.0 works correctly). So cannot test

        ObjectReader noXsiNilReader = r.without(XmlReadFeature.PROCESS_XSI_NIL);
        assertEquals(a2q("{'nil':'true'}"),
                noXsiNilReader.readValue(DOC).toString());
    }
}
