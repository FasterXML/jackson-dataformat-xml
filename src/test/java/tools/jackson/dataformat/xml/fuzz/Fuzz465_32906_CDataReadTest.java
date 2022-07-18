package tools.jackson.dataformat.xml.fuzz;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

public class Fuzz465_32906_CDataReadTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testIssue465() throws Exception
    {
        byte[] doc = readResource("/data/fuzz-32906.xml");
        try {
            JsonNode root = MAPPER.readTree(doc);
            fail("Should not pass, got: "+root);
        } catch (StreamReadException e) {
            verifyException(e, "Unexpected EOF in CDATA");
        }
    }
}
