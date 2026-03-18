package tools.jackson.dataformat.xml.lists;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#192]: Two wrapped lists with items of same name
public class WrappedListsWithSameName192Test extends XmlTestUtil
{
    static class TwoLists {
        @JacksonXmlElementWrapper(localName = "ones")
        @JacksonXmlProperty(localName = "name")
        public List<String> ones;

        @JacksonXmlElementWrapper(localName = "twos")
        @JacksonXmlProperty(localName = "name")
        public List<String> twos;
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testTwoWrappedListsWithSameItemName() throws Exception
    {
        final String xml =
                "<TwoLists>"
                + "<ones><name>one1</name><name>one2</name></ones>"
                + "<twos><name>two1</name><name>two2</name></twos>"
                + "</TwoLists>";
        TwoLists result = MAPPER.readValue(xml, TwoLists.class);
        assertNotNull(result);
        assertEquals(2, result.ones.size());
        assertEquals("one1", result.ones.get(0));
        assertEquals("one2", result.ones.get(1));
        assertEquals(2, result.twos.size());
        assertEquals("two1", result.twos.get(0));
        assertEquals("two2", result.twos.get(1));
    }
}
