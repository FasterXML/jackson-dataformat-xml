package tools.jackson.dataformat.xml.lists;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrappedListsTest extends XmlTestUtil
{
    static class Order  {
        @JacksonXmlElementWrapper(localName = "line_items")
        @JacksonXmlProperty(localName = "item")  
        private List<ListItem> line_items; // new ArrayList<ListItem>();
    }

    static class ListItem {
        public int id;
        
        public ListItem(int id) { this.id = id; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = xmlMapper(true);

    // For [Issue#103]
    @Test
    public void testEmptyList() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Order());
        assertEquals("<Order/>", xml);
        // If we expected Empty list, it'd be:
//        assertEquals("<Order><line_items/></Order>", xml);
    }
}
