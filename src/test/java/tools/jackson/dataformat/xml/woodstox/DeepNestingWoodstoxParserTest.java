package tools.jackson.dataformat.xml.woodstox;

import com.ctc.wstx.stax.WstxInputFactory;

import tools.jackson.core.JsonParser;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

public class DeepNestingWoodstoxParserTest extends XmlTestBase
{
    // Try using Woodstox-specific settings above and beyond
    // what Jackson-core would provide
    public void testDeepDocWithWoodstoxLimits() throws Exception
    {
        final WstxInputFactory wstxInputFactory = new WstxInputFactory();
        wstxInputFactory.getConfig().setMaxElementDepth(2000);
        XmlMapper xmlMapper = new XmlMapper(
                XmlFactory.builder()
                .xmlInputFactory(wstxInputFactory)
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
