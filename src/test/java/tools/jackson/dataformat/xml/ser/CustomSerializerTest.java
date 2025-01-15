package tools.jackson.dataformat.xml.ser;

import tools.jackson.core.JsonGenerator;

import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;

import tools.jackson.databind.ser.std.StdScalarSerializer;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CustomSerializerTest extends XmlTestUtil
{
    static class CustomSerializer extends StdScalarSerializer<String>
    {
        public CustomSerializer() { super(String.class); }
        
        @Override
        public void serialize(String value, JsonGenerator g,
                SerializationContext provider) {
            g.writeString("custom:"+value);
        }
    }

    // for [dataformat-xml#41]
    @Test
    public void testCustomSerializer()
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new CustomSerializer());
        final XmlMapper mapper = XmlMapper.builder()
                .addModule(module)
                .build();
        assertEquals("<String>custom:foo</String>", mapper.writeValueAsString("foo"));
    }
}
