package tools.jackson.dataformat.xml.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class DeepNestingParserTest extends XmlTestUtil
{
    @Test
    public void testDeepDoc() throws Exception
    {
        final XmlMapper xmlMapper = newMapper();
        final String XML = createDeepNestedDoc(1050);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
            fail("expected StreamReadException");
        } catch (StreamReadException e) {
            assertTrue(e.getMessage().contains("Maximum Element Depth limit (1000) Exceeded"));
        }
    }

    private String createDeepNestedDoc(final int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("<root>");
        for (int i = 0; i < depth; i++) {
            sb.append("<leaf>");
        }
        sb.append("abc");
        for (int i = 0; i < depth; i++) {
            sb.append("</leaf>");
        }
        sb.append("</root>");
        return sb.toString();
    }
}
