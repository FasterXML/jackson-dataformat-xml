package tools.jackson.dataformat.xml;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

import static org.junit.jupiter.api.Assertions.assertNotSame;

public class FeatureDefaultsTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testDeserDefaults() throws Exception
    {
        ObjectReader r = MAPPER.reader();
        assertNotSame(r, r.with(XmlReadFeature.EMPTY_ELEMENT_AS_NULL));
    }

    @Test
    public void testSerDefaults() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        assertNotSame(w, w.with(XmlWriteFeature.WRITE_XML_1_1));
    }
}
