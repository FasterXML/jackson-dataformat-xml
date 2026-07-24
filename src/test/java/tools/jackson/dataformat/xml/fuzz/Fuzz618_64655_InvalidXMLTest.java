package tools.jackson.dataformat.xml.fuzz;

import org.junit.jupiter.api.Test;

import tools.jackson.core.exc.StreamReadException;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.fail;

// [dataformat-xml#618]: Issues found by OSS-Fuzz (64655 etc)
public class Fuzz618_64655_InvalidXMLTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testWithInvalidXml1() throws Exception {
        _testWithInvalidXml(1, "Unexpected end of input", // Woodstox
                "Internal processing error by `XMLStreamReader` of type" // SJSXP
        );
    }

    @Test
    public void testWithInvalidXml2() throws Exception {
        _testWithInvalidXml(2, "Unexpected character 'a'", // Woodstox
                "Internal processing error by `XMLInputFactory` of type " // SJSXP
        );
    }

    @Test
    public void testWithInvalidXml3() throws Exception {
        _testWithInvalidXml(3, "Unexpected EOF; was expecting a close tag", // Woodstox
                "XML document structures must start and end" // SJSXP
        );
    }

    private void _testWithInvalidXml(int ix, String... errorToMatch) throws Exception
    {
        byte[] doc = readResource("/data/fuzz-618-"+ix+".xml");
        try {
            MAPPER.readTree(doc);
            // 24-Jul-2026, tatu: Must not pass silently: without this the test would
            //    also pass if no exception at all was thrown (which is how the
            //    `XMLInputFactory` guard went missing in the 3.x port, see #883)
            fail("Should not pass, invalid XML");
        } catch (StreamReadException e) {
            verifyException(e, errorToMatch);
        }
    }
}
