package tools.jackson.dataformat.xml.woodstox;

import com.ctc.wstx.stax.WstxInputFactory;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadConstraints;

import tools.jackson.dataformat.xml.*;

public class DeepNestingWoodstoxParserTest extends XmlTestUtil
{
    // Try using Woodstox-specific settings above and beyond
    // what Jackson-core would provide
    @Test
    public void testDeepDocWithWoodstoxLimits() throws Exception
    {
        final WstxInputFactory wstxInputFactory = new WstxInputFactory();
        wstxInputFactory.getConfig().setMaxElementDepth(2000);
        XmlMapper xmlMapper = new XmlMapper(
                XmlFactory.builder()
                .xmlInputFactory(wstxInputFactory)
                // match the raised Stax element-depth limit so the jackson-core
                // nesting-depth limit does not reject this deliberately deep doc
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNestingDepth(2000).build())
                .build());
        final String XML = createDeepNestedDoc(1050);
        try (JsonParser p = xmlMapper.createParser(XML)) {
            while (p.nextToken() != null) { }
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
