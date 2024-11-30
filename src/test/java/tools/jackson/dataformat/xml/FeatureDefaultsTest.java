package tools.jackson.dataformat.xml;

import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

public class FeatureDefaultsTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testDeserDefaults() throws Exception
    {
        ObjectReader r = MAPPER.reader();
        assertNotSame(r, r.with(XmlReadFeature.EMPTY_ELEMENT_AS_NULL));
    }

    public void testSerDefaults() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        assertNotSame(w, w.with(XmlWriteFeature.WRITE_XML_1_1));
    }
}
