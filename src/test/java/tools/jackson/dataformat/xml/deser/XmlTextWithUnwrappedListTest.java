package tools.jackson.dataformat.xml.deser;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Regression: a bean with @JacksonXmlText AND an unwrapped List would lose
// virtual-wrapping support because _modifyBeanDeserializer returned
// XmlTextDeserializer without also applying WrapperHandlingDeserializer.
public class XmlTextWithUnwrappedListTest extends XmlTestUtil
{
    static class Container {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> items;

        @JacksonXmlText
        public String text;
    }

    private final ObjectMapper MAPPER = newMapper();

    @Test
    public void testTextWithUnwrappedList() throws Exception {
        String xml = "<Container>some text<items>a</items><items>b</items></Container>";
        Container result = MAPPER.readValue(xml, Container.class);
        assertNotNull(result);
        assertEquals("some text", result.text);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertEquals("a", result.items.get(0));
        assertEquals("b", result.items.get(1));
    }
}
