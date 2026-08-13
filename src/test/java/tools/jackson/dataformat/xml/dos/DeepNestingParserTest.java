package tools.jackson.dataformat.xml.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.exc.StreamConstraintsException;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class DeepNestingParserTest extends XmlTestUtil
{
    // Default StreamReadConstraints.maxNestingDepth (500) is now enforced by
    // FromXmlParser itself, before the underlying Stax implementation's own
    // element-depth limit would kick in. Depth chosen just past 500 (rather
    // than e.g. 1050) so the test pins the actual enforced boundary instead
    // of merely confirming failure somewhere past both limits.
    @Test
    public void testDeepDoc() throws Exception
    {
        final XmlMapper xmlMapper = newMapper();
        final String XML = createDeepNestedDoc(510);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("nesting depth"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    // jackson-core's StreamReadConstraints.maxNestingDepth is enforced on the XML
    // read path, so a document nested past the configured limit is rejected even
    // when it stays well within the Stax element-depth limit.
    @Test
    public void testDeepDocWithLowNestingLimit() throws Exception
    {
        final XmlMapper xmlMapper = mapperBuilder(XmlFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNestingDepth(10).build())
                .build()).build();
        final String XML = createDeepNestedDoc(50);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException e) {
            assertTrue(e.getMessage().contains("nesting depth"),
                    "Unexpected message: " + e.getMessage());
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
