package tools.jackson.dataformat.xml.lists;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for [dataformat-xml#627]: null list round-trip with defaultUseWrapper(false)
 */
public class Issue627NullListTest extends XmlTestUtil
{
    @JacksonXmlRootElement(localName = "Parent")
    static class Parent {
        private List<Child> children;

        public List<Child> getChildren() { return children; }
        public void setChildren(List<Child> children) { this.children = children; }
    }

    static class Child {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Verify round-trip with defaultUseWrapper(false) and null list
    @Test
    public void testNullListRoundTripNoWrapper() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .build();

        Parent a = new Parent();
        assertNull(a.getChildren());

        String xml = mapper.writeValueAsString(a);
        assertTrue(xml.contains("xsi:nil=\"true\""),
                "Null list should serialize with xsi:nil");

        Parent b = mapper.readValue(xml, Parent.class);

        assertNull(b.getChildren(),
                "Null list should round-trip correctly with defaultUseWrapper(false)");
    }

    // Verify round-trip with default wrapper and null list (already worked)
    @Test
    public void testNullListRoundTripWithWrapper() throws Exception
    {
        XmlMapper mapper = new XmlMapper();

        Parent a = new Parent();
        String xml = mapper.writeValueAsString(a);
        Parent b = mapper.readValue(xml, Parent.class);

        assertNull(b.getChildren(),
                "Null list should round-trip correctly with default wrapper");
    }

    // Verify non-null list still works with defaultUseWrapper(false)
    @Test
    public void testNonNullListRoundTripNoWrapper() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .build();

        Parent a = new Parent();
        Child c = new Child();
        c.setName("test");
        a.setChildren(Arrays.asList(c));

        String xml = mapper.writeValueAsString(a);
        Parent b = mapper.readValue(xml, Parent.class);

        assertNotNull(b.getChildren());
        assertEquals(1, b.getChildren().size());
        assertEquals("test", b.getChildren().get(0).getName());
    }

    // Verify deserializing explicit xsi:nil on unwrapped list property
    @Test
    public void testXsiNilUnwrappedListDeser() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .build();

        String xml = "<Parent><children xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></Parent>";
        Parent b = mapper.readValue(xml, Parent.class);

        assertNull(b.getChildren(),
                "xsi:nil on unwrapped list element should produce null list");
    }
}
