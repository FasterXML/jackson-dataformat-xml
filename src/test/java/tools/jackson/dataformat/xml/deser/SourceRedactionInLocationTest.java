package tools.jackson.dataformat.xml.deser;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.DatabindException;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

// Verify that `StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` is honored, and
// that redacted locations retain line/column (see [core#1833])
public class SourceRedactionInLocationTest extends XmlTestUtil
{
    static class IntBean {
        public int a;
    }

    private final static byte[] DOC = "<IntBean><a>secret</a></IntBean>"
            .getBytes(StandardCharsets.UTF_8);

    @Test
    public void sourceRedactedByDefault() throws Exception
    {
        TokenStreamLocation loc = _failLocation(newMapper());
        assertNull(loc.contentReference().getRawContent());
        assertTrue(loc.sourceDescription().startsWith("REDACTED"));
        assertFalse(loc.toString().contains("secret"));
    }

    @Test
    public void sourceRedactedWithoutNote() throws Exception
    {
        TokenStreamLocation loc = _failLocation(mapperBuilder()
                .disable(StreamReadFeature.INCLUDE_REDACTION_NOTE_IN_LOCATION)
                .build());
        assertNull(loc.contentReference().getRawContent());
        assertEquals("", loc.sourceDescription());
    }

    @Test
    public void sourceIncludedIfEnabled() throws Exception
    {
        TokenStreamLocation loc = _failLocation(mapperBuilder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build());
        assertSame(DOC, loc.contentReference().getRawContent());
        assertTrue(loc.sourceDescription().contains("secret"));
    }

    // Line/column retained whether source is redacted or not
    private TokenStreamLocation _failLocation(XmlMapper mapper)
    {
        DatabindException e = assertThrows(DatabindException.class,
                () -> mapper.readValue(DOC, IntBean.class));
        TokenStreamLocation loc = e.getLocation();
        assertEquals("line: 1, column: 19", loc.offsetDescription());
        return loc;
    }
}
