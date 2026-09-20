package tools.jackson.dataformat.xml;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.ObjectWriteContext;

import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // Format features changed via `XmlFactory.builder()` must reach the factory
    // (and mapper built on it): they used to be collected into fields nothing read

    @Test
    void testReadFeaturesViaFactoryBuilder() throws Exception
    {
        // defaults as baseline, so checks below can not pass vacuously
        XmlFactory defaults = XmlFactory.builder().build();
        assertFalse(_enabled(defaults, XmlReadFeature.EMPTY_ELEMENT_AS_NULL));
        assertTrue(_enabled(defaults, XmlReadFeature.AUTO_DETECT_XSI_TYPE));
        assertTrue(_enabled(defaults, XmlReadFeature.PROCESS_XSI_NIL));

        XmlFactory f = XmlFactory.builder()
                .enable(XmlReadFeature.EMPTY_ELEMENT_AS_NULL)
                .disable(XmlReadFeature.AUTO_DETECT_XSI_TYPE)
                .build();
        assertTrue(_enabled(f, XmlReadFeature.EMPTY_ELEMENT_AS_NULL));
        assertFalse(_enabled(f, XmlReadFeature.AUTO_DETECT_XSI_TYPE));
        // not touched, remains as is
        assertTrue(_enabled(f, XmlReadFeature.PROCESS_XSI_NIL));

        // mapper starts from factory settings
        XmlMapper mapper = XmlMapper.builder(f).build();
        assertTrue(mapper.isEnabled(XmlReadFeature.EMPTY_ELEMENT_AS_NULL));
        assertFalse(mapper.isEnabled(XmlReadFeature.AUTO_DETECT_XSI_TYPE));
        assertTrue(mapper.isEnabled(XmlReadFeature.PROCESS_XSI_NIL));

        // and settings are not just reported but in effect, via mapper...
        final String DOC = "<root><a/></root>";
        assertEquals(Collections.singletonMap("a", ""),
                XmlMapper.builder(defaults).build().readValue(DOC, Map.class));
        assertEquals(Collections.singletonMap("a", null),
                mapper.readValue(DOC, Map.class));

        // ... as well as for parser created directly by factory
        final String XSI_DOC = "<root xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
                +" xsi:type='x'>abc</root>";
        assertEquals("xsi:type", _firstPropertyName(defaults, XSI_DOC));
        assertEquals("type", _firstPropertyName(f, XSI_DOC));
    }

    @Test
    void testReadFeaturesViaFactoryBuilderOverloads() throws Exception
    {
        XmlFactory f = XmlFactory.builder()
                .disable(XmlReadFeature.AUTO_DETECT_XSI_TYPE, XmlReadFeature.PROCESS_XSI_NIL)
                .configure(XmlReadFeature.EMPTY_ELEMENT_AS_NULL, true)
                .build();
        assertFalse(_enabled(f, XmlReadFeature.AUTO_DETECT_XSI_TYPE));
        assertFalse(_enabled(f, XmlReadFeature.PROCESS_XSI_NIL));
        assertTrue(_enabled(f, XmlReadFeature.EMPTY_ELEMENT_AS_NULL));

        // changes retained by, and may be reverted via, `rebuild()`
        assertEquals(f.getFormatReadFeatures(), f.rebuild().build().getFormatReadFeatures());
        f = f.rebuild()
                .enable(XmlReadFeature.AUTO_DETECT_XSI_TYPE, XmlReadFeature.PROCESS_XSI_NIL)
                .configure(XmlReadFeature.EMPTY_ELEMENT_AS_NULL, false)
                .build();
        assertEquals(XmlFactory.builder().build().getFormatReadFeatures(),
                f.getFormatReadFeatures());
    }

    @Test
    void testWriteFeaturesViaFactoryBuilder() throws Exception
    {
        XmlFactory defaults = XmlFactory.builder().build();
        assertFalse(_enabled(defaults, XmlWriteFeature.WRITE_XML_DECLARATION));
        assertTrue(_enabled(defaults, XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL));
        assertTrue(_enabled(defaults, XmlWriteFeature.AUTO_DETECT_XSI_TYPE));

        XmlFactory f = XmlFactory.builder()
                .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
                .disable(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL)
                .build();
        assertTrue(_enabled(f, XmlWriteFeature.WRITE_XML_DECLARATION));
        assertFalse(_enabled(f, XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL));
        // not touched, remains as is
        assertTrue(_enabled(f, XmlWriteFeature.AUTO_DETECT_XSI_TYPE));

        XmlMapper mapper = XmlMapper.builder(f).build();
        assertTrue(mapper.isEnabled(XmlWriteFeature.WRITE_XML_DECLARATION));
        assertFalse(mapper.isEnabled(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL));
        assertTrue(mapper.isEnabled(XmlWriteFeature.AUTO_DETECT_XSI_TYPE));

        // and settings are not just reported but in effect, via mapper...
        final Map<String, Object> value = Collections.singletonMap("a", null);
        assertEquals("<Map><a xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                +" xsi:nil=\"true\"/></Map>",
                XmlMapper.builder(defaults).build().writer().withRootName("Map")
                    .writeValueAsString(value));
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><Map><a/></Map>",
                mapper.writer().withRootName("Map").writeValueAsString(value));

        // ... as well as for generator created directly by factory
        assertEquals("<root/>", _writeEmptyRoot(defaults));
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><root/>", _writeEmptyRoot(f));
    }

    @Test
    void testWriteFeaturesViaFactoryBuilderOverloads() throws Exception
    {
        XmlFactory f = XmlFactory.builder()
                .enable(XmlWriteFeature.WRITE_XML_DECLARATION, XmlWriteFeature.WRITE_XML_1_1)
                .configure(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL, false)
                .build();
        assertTrue(_enabled(f, XmlWriteFeature.WRITE_XML_DECLARATION));
        assertTrue(_enabled(f, XmlWriteFeature.WRITE_XML_1_1));
        assertFalse(_enabled(f, XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL));

        // changes retained by, and may be reverted via, `rebuild()`
        assertEquals(f.getFormatWriteFeatures(), f.rebuild().build().getFormatWriteFeatures());
        f = f.rebuild()
                .disable(XmlWriteFeature.WRITE_XML_DECLARATION, XmlWriteFeature.WRITE_XML_1_1)
                .configure(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL, true)
                .build();
        assertEquals(XmlFactory.builder().build().getFormatWriteFeatures(),
                f.getFormatWriteFeatures());
    }

    private static boolean _enabled(XmlFactory f, XmlReadFeature feat) {
        return feat.enabledIn(f.getFormatReadFeatures());
    }

    private static boolean _enabled(XmlFactory f, XmlWriteFeature feat) {
        return feat.enabledIn(f.getFormatWriteFeatures());
    }

    // Name of the first property of root Object, as exposed by parser created
    // directly by the factory (no mapper-level settings involved)
    private static String _firstPropertyName(XmlFactory f, String doc) throws Exception
    {
        try (JsonParser p = f.createParser(ObjectReadContext.empty(), doc)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.PROPERTY_NAME, p.nextToken());
            return p.currentName();
        }
    }

    // Output for empty root element, as written by generator created directly
    // by the factory (no mapper-level settings involved)
    private static String _writeEmptyRoot(XmlFactory f) throws Exception
    {
        StringWriter sw = new StringWriter();
        try (JsonGenerator g = f.createGenerator(ObjectWriteContext.empty(), sw)) {
            ToXmlGenerator xg = (ToXmlGenerator) g;
            // XML declaration (if any) only written on explicit initialization
            xg.initGenerator();
            xg.setNextName(new QName("root"));
            g.writeStartObject();
            g.writeEndObject();
        }
        return sw.toString();
    }
}
