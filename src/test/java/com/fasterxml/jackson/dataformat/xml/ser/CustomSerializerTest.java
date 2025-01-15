package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomSerializerTest extends XmlTestUtil
{
    @SuppressWarnings("serial")
    static class CustomSerializer extends StdScalarSerializer<String>
    {
        public CustomSerializer() { super(String.class); }
        
        @Override
        public void serialize(String value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException {
            jgen.writeString("custom:"+value);
        }
    }
    
    // for [dataformat-xml#41]
    @Test
    public void testCustomSerializer() throws Exception
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new CustomSerializer());
        final XmlMapper mapper = XmlMapper.builder()
                .addModule(module)
                .build();
        assertEquals("<String>custom:foo</String>", mapper.writeValueAsString("foo"));
    }
}
