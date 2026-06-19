package tools.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for [dataformat-xml#871]: an `xsi:nil="true"` collection element on an unwrapped
 * collection must read as a single null *element* within the collection, not as a null
 * collection (regression from the original #627 attempt).
 */
public class Issue871NullElementTest extends XmlTestUtil
{
    @JsonRootName(value = "Collection")
    static class CollectionWrapper {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "e")
        public Collection<String> value;

        public CollectionWrapper() { this.value = new ArrayList<>(); }
    }

    private final XmlMapper MAPPER = new XmlMapper();

    private static final String NS = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";

    @Test
    public void testSingleNullElement() throws Exception
    {
        String xml = "<Collection><e " + NS + " xsi:nil=\"true\"/></Collection>";
        CollectionWrapper cw = MAPPER.readValue(xml, CollectionWrapper.class);

        assertNotNull(cw.value);
        assertEquals(1, cw.value.size());
        assertNull(cw.value.iterator().next());
    }

    @Test
    public void testLeadingNullElementNotDropped() throws Exception
    {
        String xml = "<Collection><e " + NS + " xsi:nil=\"true\"/><e>x</e></Collection>";
        CollectionWrapper cw = MAPPER.readValue(xml, CollectionWrapper.class);

        assertNotNull(cw.value);
        assertEquals(2, cw.value.size());
        assertEquals(java.util.Arrays.asList(null, "x"), new ArrayList<>(cw.value));
    }
}
