package tools.jackson.dataformat.xml.deser.creator;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class NestedSingleArgCtors547Test extends XmlTestUtil
{
    private static final XmlMapper XML_MAPPER = newMapper();

    static class Outer547Del {
        public Inner547Del inner;
    }

    static class Inner547Del {
        protected String value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Inner547Del(@JsonProperty("value") String v) {
            value = v;
        }
    }

    static class Outer547Props {
        public Inner547Props inner;
    }

    static class Inner547Props {
        protected String value;

        // 20-Nov-2022, tatu: [dataformat-xml#547] Shouldn't need annotation
//        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Inner547Props(@JsonProperty("value") String v) {
            value = v;
        }
    }

    // [dataformat-xml#547]
    @Test
    public void testNested1ArgCtorsDelegating() throws Exception
    {
        String xml = "<outer><inner></inner></outer>";
        Outer547Del result = XML_MAPPER.readValue(xml, Outer547Del.class);
        assertNotNull(result.inner);
        assertEquals("", result.inner.value);
    }

    // [dataformat-xml#547]
    @Test
    public void testNested1ArgCtorsProps() throws Exception
    {
        String xml = "<outer><inner></inner></outer>";
        Outer547Props result = XML_MAPPER.readValue(xml, Outer547Props.class);
        assertNotNull(result.inner);
        assertNull(result.inner.value);
    }
}
