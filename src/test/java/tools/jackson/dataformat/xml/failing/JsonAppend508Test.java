package tools.jackson.dataformat.xml.failing;

import tools.jackson.core.JsonGenerator;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonAppend;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.ser.VirtualBeanPropertyWriter;
import tools.jackson.databind.util.Annotations;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;

public class JsonAppend508Test extends XmlTestBase
{
    // [dataformat-xml#508]: Duplication of virtual properties
    @JsonAppend(props = @JsonAppend.Prop(name = "virtual", value = MyVirtualPropertyWriter.class))
    public static class Pojo508 {
        private final String name;

        public Pojo508(String name) {
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

    // [dataformat-xml#508]: Duplication of virtual properties
    public void testJsonAppend() throws Exception {
        String xml = MAPPER.writeValueAsString(new Pojo508("foo"));
        assertEquals("<Pojo><name>foo</name><virtual>bar</virtual></Pojo>",xml);
    }
}
