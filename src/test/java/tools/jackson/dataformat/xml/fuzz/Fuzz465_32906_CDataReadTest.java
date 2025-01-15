package tools.jackson.dataformat.xml.fuzz;

import org.junit.jupiter.api.Test;

import tools.jackson.core.exc.StreamReadException;

import tools.jackson.databind.JsonNode;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class Fuzz465_32906_CDataReadTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
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
