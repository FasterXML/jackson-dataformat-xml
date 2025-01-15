package tools.jackson.dataformat.xml.ser;

import tools.jackson.core.JsonGenerator;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonAppend;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.ser.VirtualBeanPropertyWriter;
import tools.jackson.databind.util.Annotations;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonAppend578Test extends XmlTestUtil
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
        protected Object value(Object bean, JsonGenerator g, SerializationContext ctxt) {
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
    @Test
    public void testJsonAppend() throws Exception {
        String xml = MAPPER.writeValueAsString(new Pojo578("foo"));
        assertEquals("<Pojo578><name>foo</name><virtual>bar</virtual></Pojo578>",xml);
    }
}
