package tools.jackson.dataformat.xml;

import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

import tools.jackson.dataformat.xml.deser.FromXmlParser;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

public class FeatureDefaultsTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testDeserDefaults() throws Exception
    {
        ObjectReader r = MAPPER.reader();
        assertNotSame(r, r.with(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));
    }

    public void testSerDefaults() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        assertNotSame(w, w.with(ToXmlGenerator.Feature.WRITE_XML_1_1));
    }
}
