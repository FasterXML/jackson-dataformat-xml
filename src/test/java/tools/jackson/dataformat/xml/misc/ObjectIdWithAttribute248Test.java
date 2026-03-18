package tools.jackson.dataformat.xml.misc;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#248]: @JacksonXmlProperty(isAttribute=true) with @JsonIdentityInfo
//  throws "Trying to write an attribute when there is no open start element"
public class ObjectIdWithAttribute248Test extends XmlTestUtil
{
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    static class NodeA {
        @JacksonXmlProperty(isAttribute = true)
        public String name;

        public NodeB ref;

        public NodeA() { }
        public NodeA(String name) { this.name = name; }
    }

    static class NodeB {
        public NodeA ref;

        public NodeB() { }
        public NodeB(NodeA a) { this.ref = a; }
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testSerializeAttributeWithIdentityInfo() throws Exception
    {
        NodeA a = new NodeA("test");
        NodeB b = new NodeB(a);
        a.ref = b;

        String xml = MAPPER.writeValueAsString(a);
        assertNotNull(xml);
        assertTrue(xml.contains("name=\"test\""),
                "Expected 'name' as XML attribute, got: " + xml);
    }

    @Test
    public void testRoundTripAttributeWithIdentityInfo() throws Exception
    {
        NodeA a = new NodeA("test");
        NodeB b = new NodeB(a);
        a.ref = b;

        String xml = MAPPER.writeValueAsString(a);
        NodeA result = MAPPER.readValue(xml, NodeA.class);

        assertNotNull(result);
        assertEquals("test", result.name);
        assertNotNull(result.ref);
        assertSame(result, result.ref.ref);
    }
}
