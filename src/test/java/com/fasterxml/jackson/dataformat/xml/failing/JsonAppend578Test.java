package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class JsonAppend578Test extends XmlTestBase
{
    // [dataformat-xml#578]: Duplication of virtual properties
    @JsonAppend(props = @JsonAppend.Prop(name = "virtual", value = MyVirtualPropertyWriter.class))
    public static class Pojo578 {
        private final String name;

        public Pojo578(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    static class MyVirtualPropertyWriter extends VirtualBeanPropertyWriter {
        private static final long serialVersionUID = 1L;

        public MyVirtualPropertyWriter() {}

        protected MyVirtualPropertyWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations,
                                          JavaType declaredType) {
            super(propDef, contextAnnotations, declaredType);
        }

        @Override
        protected Object value(Object bean, JsonGenerator g, SerializerProvider prov) {
            return "bar";
        }

        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass,
                BeanPropertyDefinition propDef, JavaType type) {
            return new MyVirtualPropertyWriter(propDef, declaringClass.getAnnotations(), type);
        }
    }

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#578]: Duplication of virtual properties
    public void testJsonAppend() throws Exception {
        String xml = MAPPER.writeValueAsString(new Pojo578("foo"));
        assertEquals("<Pojo><name>foo</name><virtual>bar</virtual></Pojo>",xml);
    }
}
