package tools.jackson.dataformat.xml.stream;

import java.util.Map;

import javax.xml.stream.XMLInputFactory;

import org.junit.jupiter.api.Test;

import com.ctc.wstx.stax.WstxInputFactory;

import tools.jackson.core.exc.StreamReadException;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Readers configured with `IS_REPLACING_ENTITY_REFERENCES` disabled report
// general entity references in text content as `ENTITY_REFERENCE` events.
// `XmlTokenStream` used to ignore those while collecting text, silently
// dropping part of the content: either the replacement text has to be used,
// or (if the reader has none to offer) reading must fail.
public class EntityReferenceReadTest extends XmlTestUtil
{
    private final static String DOC_INTERNAL_ENTITY =
            "<!DOCTYPE root [<!ENTITY e 'xx'>]>\n"
            +"<root><a>foo&e;bar</a></root>";

    private final static String DOC_UNDECLARED_ENTITY =
            "<root><a>foo&e;bar</a></root>";

    // Text collected after an END_ELEMENT goes through a different code path
    // (mixed content), so cover that one too
    private final static String DOC_MIXED_CONTENT =
            "<!DOCTYPE root [<!ENTITY e 'xx'>]>\n"
            +"<root><a>1</a>foo&e;bar<b>2</b></root>";

    // System id deliberately points nowhere: a non-replacing reader must never
    // try to resolve it
    private final static String DOC_EXTERNAL_ENTITY =
            "<!DOCTYPE root [<!ENTITY ext SYSTEM 'file:///nonexistent/entity.txt'>]>\n"
            +"<root><a>foo&ext;bar</a></root>";

    // Replacement text of `b` refers to another entity
    private final static String DOC_NESTED_ENTITY =
            "<!DOCTYPE root [<!ENTITY a 'x'><!ENTITY b '&a;&a;'>]>\n"
            +"<root><a>foo&b;bar</a></root>";

    private final XmlMapper NON_REPLACING_MAPPER = _nonReplacingMapper(true);

    private final XmlMapper NON_REPLACING_NO_DTD_MAPPER = _nonReplacingMapper(false);

    private static XmlMapper _nonReplacingMapper(boolean supportDTD) {
        XMLInputFactory f = new WstxInputFactory();
        f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        f.setProperty(XMLInputFactory.SUPPORT_DTD, supportDTD);
        return mapperBuilder(XmlFactory.builder().xmlInputFactory(f).build()).build();
    }

    @Test
    public void testDeclaredEntityNotReplacedByReader() throws Exception
    {
        Map<?,?> result = NON_REPLACING_MAPPER.readValue(DOC_INTERNAL_ENTITY, Map.class);
        assertEquals("fooxxbar", result.get("a"));
    }

    @Test
    public void testDeclaredEntityNotReplacedByReaderMixedContent() throws Exception
    {
        Map<?,?> result = NON_REPLACING_MAPPER.readValue(DOC_MIXED_CONTENT, Map.class);
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
        assertEquals("fooxxbar", result.get(""));
    }

    @Test
    public void testUndeclaredEntityNotReplacedByReader() throws Exception
    {
        // No DTD, so no replacement text for the reader to hand out: must fail,
        // not quietly produce "foobar"
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> NON_REPLACING_NO_DTD_MAPPER.readValue(DOC_UNDECLARED_ENTITY, Map.class));
        verifyException(e, "Unexpanded entity reference '&e;'");
    }

    @Test
    public void testExternalEntityNotReplacedByReader() throws Exception
    {
        // A non-replacing reader reports an external entity reference without
        // replacement text (it does not resolve the system id): must fail the
        // same way as an undeclared one, never exposing the referenced content
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> NON_REPLACING_MAPPER.readValue(DOC_EXTERNAL_ENTITY, Map.class));
        verifyException(e, "Unexpanded entity reference '&ext;'");
    }

    @Test
    public void testNestedEntityNotExpandedByTokenStream() throws Exception
    {
        // Only the reader's own (one level) replacement text is used, verbatim:
        // the token stream expands nothing itself, so there is no amplification
        // beyond what the reader already does
        Map<?,?> result = NON_REPLACING_MAPPER.readValue(DOC_NESTED_ENTITY, Map.class);
        assertEquals("foo&a;&a;bar", result.get("a"));
    }

    @Test
    public void testDefaultReaderStillReplaces() throws Exception
    {
        // Default (replacing) reader with DTD support enabled expands the entity
        // itself; no change in behavior there
        XMLInputFactory f = new WstxInputFactory();
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        f.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        XmlMapper mapper = mapperBuilder(XmlFactory.builder().xmlInputFactory(f).build()).build();
        Map<?,?> result = mapper.readValue(DOC_INTERNAL_ENTITY, Map.class);
        assertEquals("fooxxbar", result.get("a"));
    }
}
