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
public class PolymorphicConvertValue905Test extends XmlTestUtil
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

    // [dataformat-xml#905]: same problem via `_serializeObjectId()`, which is
    // taken instead of the above when bean also has Object Id
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({ @JsonSubTypes.Type(value = Employee.class, name = "emp") })
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class,
            property = "id")
    static abstract class Person {
        public String name;
    }

    static class Employee extends Person {
        public Person manager;

        public Employee() { }
        public Employee(String n) { name = n; }
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

    // Object Id + type id: `_serializeObjectId()` must skip attribute handling too
    @Test
    public void testObjectIdValueToTree() throws Exception
    {
        Employee boss = new Employee("Boss");
        boss.manager = boss;

        JsonNode tree = MAPPER.valueToTree(boss);
        assertEquals("emp", tree.path("_type").asString());
        assertEquals(1, tree.path("id").asInt());
        assertEquals("Boss", tree.path("name").asString());
        // self-reference written as plain Object Id
        assertEquals(1, tree.path("manager").asInt());
    }

    @Test
    public void testObjectIdConvertValueToMap() throws Exception
    {
        Employee boss = new Employee("Boss");
        boss.manager = boss;

        @SuppressWarnings("unchecked")
        Map<String, Object> map = MAPPER.convertValue(boss, Map.class);
        assertEquals("emp", map.get("_type"));
        assertEquals(1, map.get("id"));
        assertEquals("Boss", map.get("name"));
        assertEquals(1, map.get("manager"));
    }

    // And real XML serialization of Object Id bean must still use attribute
    @Test
    public void testObjectIdXmlSerializationUnchanged() throws Exception
    {
        Employee boss = new Employee("Boss");
        boss.manager = boss;

        String xml = MAPPER.writeValueAsString(boss);
        assertTrue(xml.contains("_type=\"emp\""), "should write type id as attribute: " + xml);
    }
}
