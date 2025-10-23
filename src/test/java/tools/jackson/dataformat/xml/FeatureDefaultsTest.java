package tools.jackson.dataformat.xml;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // [dataformat-xml#780]
    @Test
    void testFeaturesViaMapper() {
        XmlMapper mapper = XmlMapper.shared();
        assertTrue(mapper.isEnabled(XmlReadFeature.AUTO_DETECT_XSI_TYPE));
        assertTrue(mapper.isEnabled(XmlWriteFeature.AUTO_DETECT_XSI_TYPE));
        
    }
}
