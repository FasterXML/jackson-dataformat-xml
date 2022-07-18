package tools.jackson.dataformat.xml.ser;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializerProvider;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

public class CustomSerializerTest extends XmlTestBase
{
    static class CustomSerializer extends StdScalarSerializer<String>
    {
        public CustomSerializer() { super(String.class); }
        
        @Override
        public void serialize(String value, JsonGenerator jgen,
                SerializerProvider provider) {
            jgen.writeString("custom:"+value);
        }
    }
    
    // for [dataformat-xml#41]
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
