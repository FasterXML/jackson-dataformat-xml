package tools.jackson.dataformat.xml.ser;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

// Polymorphic beans (default `As.PROPERTY` inclusion) failed with a
// `ClassCastException` when run through `convertValue()`/`valueToTree()`,
// because those serialize into a `TokenBuffer`/tree generator rather than a
// `ToXmlGenerator`, which the type-id handling cast to unconditionally.
public class PolymorphicConvertValueTest extends XmlTestUtil
{
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({ @JsonSubTypes.Type(value = Dog.class, name = "dog") })
    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public int barks;

        public Dog() { }
        public Dog(String n, int b) { name = n; barks = b; }
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testValueToTree() throws Exception
    {
        Animal input = new Dog("Rex", 3);
        JsonNode tree = MAPPER.valueToTree(input);
        assertEquals("dog", tree.path("_type").asString());
        assertEquals("Rex", tree.path("name").asString());
        assertEquals(3, tree.path("barks").asInt());
    }

    @Test
    public void testConvertValueToMap() throws Exception
    {
        Animal input = new Dog("Rex", 3);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = MAPPER.convertValue(input, Map.class);
        assertEquals("dog", map.get("_type"));
        assertEquals("Rex", map.get("name"));
        assertEquals(3, map.get("barks"));
    }

    // Real XML serialization must be unchanged: type id still written as attribute
    @Test
    public void testXmlSerializationUnchanged() throws Exception
    {
        Animal input = new Dog("Rex", 3);
        String xml = MAPPER.writeValueAsString(input);
        assertTrue(xml.contains("_type=\"dog\""), "should write type id as attribute: " + xml);
    }
}
