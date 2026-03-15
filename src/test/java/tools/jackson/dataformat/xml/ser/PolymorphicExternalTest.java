package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#802] Test for EXTERNAL_PROPERTY type info duplication
public class PolymorphicExternalTest extends XmlTestUtil
{
    static class Cage {
        public String id;
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
        @JsonSubTypes({
                @JsonSubTypes.Type(value = Cat.class, name = "CAT"),
                @JsonSubTypes.Type(value = Dog.class, name = "DOG")
        })
        public Animal animal;
    }

    public abstract static class Animal { }

    public static class Cat extends Animal {
        public String firstName = "My name is cat";
    }

    public static class Dog extends Animal {
        public String lastName = "My name is dog";
    }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testExternalPropertyNoDuplicate() throws Exception
    {
        Cage cage = new Cage();
        cage.id = "123";
        cage.type = "CAT";
        cage.animal = new Cat();

        String xml = MAPPER.writeValueAsString(cage);
        System.out.println("Serialized XML: " + xml);

        // Count occurrences of "<type>" - should be exactly 1
        int count = countOccurrences(xml, "<type>");
        assertEquals(1, count,
                "Expected exactly one <type> element but found " + count + " in: " + xml);
    }

    @Test
    public void testExternalPropertyRoundTrip() throws Exception
    {
        Cage cage = new Cage();
        cage.id = "123";
        cage.type = "CAT";
        cage.animal = new Cat();

        String xml = MAPPER.writeValueAsString(cage);
        System.out.println("Serialized XML: " + xml);

        // Should be able to round-trip
        Cage result = MAPPER.readValue(xml, Cage.class);
        assertNotNull(result);
        assertEquals("123", result.id);
        // Note: with EXTERNAL_PROPERTY, the external type handler consumes
        // the "type" element for type resolution, so the bean property is null
        // (same behavior as JSON)
        assertInstanceOf(Cat.class, result.animal);
        assertEquals("My name is cat", ((Cat) result.animal).firstName);
    }

    private static int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
