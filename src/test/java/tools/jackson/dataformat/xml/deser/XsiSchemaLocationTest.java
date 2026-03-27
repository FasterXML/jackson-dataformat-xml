package tools.jackson.dataformat.xml.deser;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#358]: XSI namespace attributes like schemaLocation
// should be silently ignored when SKIP_UNKNOWN_XSI_ATTRIBUTES is enabled
public class XsiSchemaLocationTest extends XmlTestUtil
{
    static class Dto {
        public String value;
    }

    static class DtoWithAttr {
        public String value;
        public int count;
    }

    static class DtoWithSchemaLocation {
        public String value;
        public String schemaLocation;
    }

    private final XmlMapper MAPPER_SKIP = mapperBuilder()
            .enable(XmlReadFeature.SKIP_UNKNOWN_XSI_ATTRIBUTES)
            .build();

    private final XmlMapper MAPPER_DEFAULT = mapperBuilder().build();

    // Basic case from issue #358: xsi:schemaLocation should be ignored when enabled
    @Test
    public void testXsiSchemaLocationIgnored() throws Exception
    {
        Dto result = MAPPER_SKIP.readValue(
                "<dto xmlns=\"http://SomeNamespace\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://SomeNamespace"
                + " http://SomeNamespace/schema.xsd\">"
                + "<value>hello</value>"
                + "</dto>",
                Dto.class);
        assertEquals("hello", result.value);
    }

    // Also test xsi:noNamespaceSchemaLocation
    @Test
    public void testXsiNoNamespaceSchemaLocationIgnored() throws Exception
    {
        Dto result = MAPPER_SKIP.readValue(
                "<dto xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:noNamespaceSchemaLocation=\"schema.xsd\">"
                + "<value>world</value>"
                + "</dto>",
                Dto.class);
        assertEquals("world", result.value);
    }

    // Ensure regular attributes still work alongside XSI attributes
    @Test
    public void testXsiSchemaLocationWithRegularAttributes() throws Exception
    {
        DtoWithAttr result = MAPPER_SKIP.readValue(
                "<DtoWithAttr xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://SomeNamespace schema.xsd\""
                + " count=\"42\">"
                + "<value>test</value>"
                + "</DtoWithAttr>",
                DtoWithAttr.class);
        assertEquals("test", result.value);
        assertEquals(42, result.count);
    }

    // When feature is disabled (default), XSI attributes should be exposed
    // and bindable to POJO properties
    @Test
    public void testXsiSchemaLocationExposedByDefault() throws Exception
    {
        DtoWithSchemaLocation result = MAPPER_DEFAULT.readValue(
                "<DtoWithSchemaLocation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://SomeNamespace schema.xsd\">"
                + "<value>bound</value>"
                + "</DtoWithSchemaLocation>",
                DtoWithSchemaLocation.class);
        assertEquals("bound", result.value);
        assertEquals("http://SomeNamespace schema.xsd", result.schemaLocation);
    }
}
