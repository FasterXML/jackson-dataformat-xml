package tools.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.core.*;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test(s) for [dataformat-xml#42], problems with custom (de)serializer.
 */
public class TestSerializerCustom extends XmlTestUtil
{
    @JsonPropertyOrder({ "name", "obj" })
    static class Item {
        public String name;
        public Foo obj;
        public Item(String name, Foo obj) {
          this.name = name;
          this.obj = obj;
        }
    }

    static class Foo {
        public String name;
        protected Foo() { }
        public Foo(String name) {
          this.name = name;
        }
    }

    static class ItemDeserializer extends StdDeserializer<Item> {
        public ItemDeserializer() {
          super(Item.class);
        }

        @Override
        public Item deserialize(JsonParser p, DeserializationContext ctxt) {
          JsonNode json = ctxt.readTree(p);
          JsonNode foo = json.get("obj");
          if (foo == null) {
              throw new IllegalStateException("missing foo property");
          }
          return new Item(json.path("name").asString(),
                  new Foo(foo.path("name").asString()));
        }
    }

    public class ItemSerializer extends StdSerializer<Item> {
        public ItemSerializer() {
          super(Item.class);
        }

        @Override
        public void serialize(Item value, JsonGenerator g, SerializationContext ctxt) {
          g.writeStartObject();
          g.writePOJOProperty("obj", value.obj);
          g.writeStringProperty("name", value.name);
          g.writeEndObject();
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    @Test
    public void testIssue42() throws Exception
    {
        SimpleModule m = new SimpleModule("module", new Version(1,0,0,null,null,null));
        m.addSerializer(Item.class, new ItemSerializer());
        m.addDeserializer(Item.class, new ItemDeserializer());
        XmlMapper xmlMapper = XmlMapper.builder()
                .addModule(m)
                .build();
        Item value = new Item("itemName", new Foo("fooName"));
        String xml = xmlMapper.writeValueAsString(value);
        
        Item result = xmlMapper.readValue(xml, Item.class);
        assertNotNull(result);
        assertEquals("itemName", result.name);
        assertNotNull(result.obj);
        assertEquals("fooName", result.obj.name);
    }
}
