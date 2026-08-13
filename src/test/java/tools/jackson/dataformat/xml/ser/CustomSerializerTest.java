package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;

import tools.jackson.databind.ser.std.StdScalarSerializer;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class CustomSerializerTest extends XmlTestUtil
{
    // for [dataformat-xml#41]
    static class CustomSerializer41 extends StdScalarSerializer<String>
    {
        public CustomSerializer41() { super(String.class); }
        
        @Override
        public void serialize(String value, JsonGenerator g,
                SerializationContext provider) {
            g.writeString("custom:"+value);
        }
    }

    // [dataformat-xml#42]
    @JsonPropertyOrder({ "name", "obj" })
    public static class Item42 {
        public String name;
        public Foo obj;
        public Item42(String name, Foo obj) {
            this.name = name;
            this.obj = obj;
        }
    }

    public static class Foo {
        public String name;
        protected Foo() { }
        public Foo(String name) {
            this.name = name;
        }
    }

    // [dataformat-xml#42]
    static class ItemDeserializer42 extends StdDeserializer<Item42> {
        public ItemDeserializer42() {
            super(Item42.class);
        }

        @Override
        public Item42 deserialize(JsonParser p, DeserializationContext ctxt) {
            JsonNode json = ctxt.readTree(p);
            JsonNode foo = json.get("obj");
            if (foo == null) {
                throw new IllegalStateException("missing foo property");
            }
            return new Item42(json.path("name").asString(),
                    new Foo(foo.path("name").asString()));
        }
    }

    // [dataformat-xml#42]
    public class ItemSerializer42 extends StdSerializer<Item42> {
        public ItemSerializer42() {
          super(Item42.class);
        }

        @Override
        public void serialize(Item42 value, JsonGenerator g, SerializationContext ctxt) {
            g.writeStartObject();
            g.writePOJOProperty("obj", value.obj);
            g.writeStringProperty("name", value.name);
            g.writeEndObject();
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    // for [dataformat-xml#41]
    @Test
    public void testCustomSerializer()
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new CustomSerializer41());
        final XmlMapper mapper = XmlMapper.builder()
                .addModule(module)
                .build();
        assertEquals("<String>custom:foo</String>", mapper.writeValueAsString("foo"));
    }

    // [dataformat-xml#42]
    @Test
    public void testCustomSerializer42() throws Exception
    {
        SimpleModule m = new SimpleModule("module", new Version(1,0,0,null,null,null));
        m.addSerializer(Item42.class, new ItemSerializer42());
        m.addDeserializer(Item42.class, new ItemDeserializer42());
        XmlMapper xmlMapper = XmlMapper.builder()
                .addModule(m)
                .build();
        Item42 value = new Item42("itemName", new Foo("fooName"));
        String xml = xmlMapper.writeValueAsString(value);
        
        Item42 result = xmlMapper.readValue(xml, Item42.class);
        assertNotNull(result);
        assertEquals("itemName", result.name);
        assertNotNull(result.obj);
        assertEquals("fooName", result.obj.name);
    }
}
