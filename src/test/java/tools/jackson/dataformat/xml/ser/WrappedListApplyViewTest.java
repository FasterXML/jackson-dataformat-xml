package tools.jackson.dataformat.xml.ser;

import java.util.*;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonApplyView;
import com.fasterxml.jackson.annotation.JsonView;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

// Verifies @JsonApplyView (i.e. _applyView) is honored for WRAPPED collection
// properties: the forced view must propagate to the collection's element beans,
// same as BeanPropertyWriter.serializeAsProperty() does.
public class WrappedListApplyViewTest extends XmlTestUtil
{
    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    static class Item {
        @JsonView(Views.Public.class)
        public String pub = "A";
        @JsonView(Views.Internal.class)
        public String internalOnly = "SECRET";
    }

    @JacksonXmlRootElement(localName = "Bean")
    static class Bean {
        // Force Public view onto this list and its elements, regardless of active view
        @JsonApplyView(Views.Public.class)
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        public List<Item> items = new ArrayList<>(Arrays.asList(new Item()));
    }

    @Test
    public void testApplyViewPropagatesToElements() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        // No active view set. Without _applyView handling the active view stays null
        // (no filtering) so BOTH 'pub' and 'internalOnly' are written. With it, Public
        // view is forced -> 'pub' kept, 'internalOnly' (Internal-only) excluded.
        String xml = mapper.writeValueAsString(new Bean());
        assertTrue(xml.contains("<pub>"), "expected 'pub' element: " + xml);
        assertFalse(xml.contains("internalOnly"),
                "@JsonApplyView(Public) must exclude Internal-only nested prop: " + xml);
    }
}
