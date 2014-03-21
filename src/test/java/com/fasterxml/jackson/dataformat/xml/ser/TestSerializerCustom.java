package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Unit test(s) for [Issue#42], problems with custom (de)serializer.
 */
@SuppressWarnings("serial")
public class TestSerializerCustom extends XmlTestBase
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
        public Item deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
          ObjectCodec oc = jp.getCodec();
          JsonNode json = oc.readTree(jp);
          JsonNode foo = json.get("obj");
          if (foo == null) {
              throw new IllegalStateException("missing foo property");
          }
          return new Item(json.path("name").asText(),
                  oc.treeToValue(foo, Foo.class));
        }
    }

    public class ItemSerializer extends StdSerializer<Item> {
        public ItemSerializer() {
          super(Item.class);
        }

        @Override
        public void serialize(Item value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
          jgen.writeStartObject();
          jgen.writeObjectField("obj", value.obj);
          jgen.writeStringField("name", value.name);
          jgen.writeEndObject();
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testIssue42() throws Exception
    {
        XmlMapper xmlMapper = new XmlMapper();
        SimpleModule m = new SimpleModule("module", new Version(1,0,0,null,null,null));
        m.addSerializer(Item.class, new ItemSerializer());
        m.addDeserializer(Item.class, new ItemDeserializer());
        xmlMapper.registerModule(m);

        Item value = new Item("itemName", new Foo("fooName"));
        String xml = xmlMapper.writeValueAsString(value);
        
        Item result = xmlMapper.readValue(xml, Item.class);
        assertNotNull(result);
        assertEquals("itemName", result.name);
        assertNotNull(result.obj);
        assertEquals("fooName", result.obj.name);
    }
}
