package tools.jackson.dataformat.xml.misc;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlNameProcessor;
import tools.jackson.dataformat.xml.XmlNameProcessors;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Virtual wrapping of unwrapped (repeated) elements matches the incoming
// element name against a name stored after the XmlNameProcessor has decoded
// it; the match itself must therefore use the decoded name too.
public class XmlNameProcessorUnwrappedListTest extends XmlTestUtil
{
    static class Bean {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> lists;
    }

    private XmlMapper mapperWith(XmlNameProcessor proc) {
        return XmlMapper.builder(
                XmlFactory.builder().xmlNameProcessor(proc).build()
        ).build();
    }

    @Test
    public void testAlwaysOnBase64RoundTrip() throws Exception {
        XmlMapper mapper = mapperWith(XmlNameProcessors.newAlwaysOnBase64Processor());
        Bean bean = new Bean();
        bean.lists = List.of("a", "b", "c");

        String xml = mapper.writeValueAsString(bean);
        Bean back = mapper.readValue(xml, Bean.class);

        assertNotNull(back.lists);
        assertEquals(List.of("a", "b", "c"), back.lists);
    }

    @Test
    public void testBase64EncodedNameRoundTrip() throws Exception {
        // "lists" is a valid XML name, so it only gets base64-escaped once it
        // collides with the prefix; use a custom prefix that forces escaping.
        XmlMapper mapper = mapperWith(XmlNameProcessors.newBase64Processor("lists"));
        Bean bean = new Bean();
        bean.lists = List.of("x", "y", "z");

        String xml = mapper.writeValueAsString(bean);
        Bean back = mapper.readValue(xml, Bean.class);

        assertNotNull(back.lists);
        assertEquals(List.of("x", "y", "z"), back.lists);
    }
}
