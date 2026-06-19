package tools.jackson.dataformat.xml.ser;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonApplyView;
import com.fasterxml.jackson.annotation.JsonView;

import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for [dataformat-xml#873]: {@code @JsonApplyView} (i.e. the {@code _applyView}
 * mechanism) must be honored for Collection/array properties, both wrapped and unwrapped,
 * so that the forced View propagates to the collection's element beans -- same as
 * {@code BeanPropertyWriter.serializeAsProperty()} does for scalar properties.
 */
public class ListApplyView873Test extends XmlTestUtil
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

    // Wrapped list with a forced View (Public)
    @JacksonXmlRootElement(localName = "Bean")
    static class WrappedForcedBean {
        @JsonApplyView(Views.Public.class)
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        public List<Item> items = new ArrayList<>(Arrays.asList(new Item()));
    }

    // Wrapped list with View processing explicitly disabled (NONE)
    @JacksonXmlRootElement(localName = "Bean")
    static class WrappedNoneBean {
        // `@JsonView` so the property itself survives an active Public view;
        // `@JsonApplyView(NONE)` then disables View filtering for the elements.
        @JsonView(Views.Public.class)
        @JsonApplyView(JsonApplyView.NONE.class)
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        public List<Item> items = new ArrayList<>(Arrays.asList(new Item()));
    }

    // Unwrapped list with a forced View (Public)
    @JacksonXmlRootElement(localName = "Bean")
    static class UnwrappedForcedBean {
        @JsonApplyView(Views.Public.class)
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        public List<Item> items = new ArrayList<>(Arrays.asList(new Item()));
    }

    private final XmlMapper MAPPER = new XmlMapper();

    // Wrapped: @JsonApplyView(Public) forces Public view onto elements even with no
    // active view set -> 'pub' kept, Internal-only 'internalOnly' excluded.
    @Test
    public void testForcedViewWrapped() throws Exception
    {
        JsonNode item = itemNode(MAPPER.writeValueAsString(new WrappedForcedBean()));
        assertEquals("A", item.path("pub").asString(null));
        assertTrue(item.path("internalOnly").isMissingNode(),
                "Internal-only property must be excluded by forced Public view: " + item);
    }

    // Wrapped: @JsonApplyView(NONE) disables view filtering for elements, so even with
    // an active Public view BOTH properties are written.
    @Test
    public void testNoneViewWrapped() throws Exception
    {
        String xml = MAPPER.writerWithView(Views.Public.class)
                .writeValueAsString(new WrappedNoneBean());
        JsonNode item = itemNode(xml);
        assertEquals("A", item.path("pub").asString(null));
        assertEquals("SECRET", item.path("internalOnly").asString(null),
                "@JsonApplyView(NONE) must disable view filtering for elements: " + item);
    }

    // Unwrapped list goes through a different writer path; verify it honors the forced
    // view too (regression guard, since the forced view is the whole point of #873).
    @Test
    public void testForcedViewUnwrapped() throws Exception
    {
        JsonNode item = MAPPER.readTree(MAPPER.writeValueAsString(new UnwrappedForcedBean()))
                .path("item");
        assertEquals("A", item.path("pub").asString(null));
        assertTrue(item.path("internalOnly").isMissingNode(),
                "Internal-only property must be excluded by forced Public view (unwrapped): " + item);
    }

    // For wrapped output: <Bean><items><item>...</item></items></Bean>
    private JsonNode itemNode(String xml) {
        return MAPPER.readTree(xml).path("items").path("item");
    }
}
