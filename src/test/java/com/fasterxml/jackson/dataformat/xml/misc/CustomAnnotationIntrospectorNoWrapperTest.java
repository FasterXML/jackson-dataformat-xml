package com.fasterxml.jackson.dataformat.xml.misc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * A regression test for https://github.com/FasterXML/jackson-databind/issues/4595
 */
public class CustomAnnotationIntrospectorNoWrapperTest extends XmlTestBase
{
    public static class Foo {
        private final List<String> bar;

        public Foo(List<String> bar) {
            this.bar = bar;
        }

        @NoWrapper
        public List<String> getBar() {
            return bar;
        }
    }

    public static class NoWrapperIntrospector extends NopAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public PropertyName findWrapperName(Annotated ann) {
            if (ann.hasAnnotation(NoWrapper.class)) {
                return PropertyName.NO_NAME;
            }
            return super.findWrapperName(ann);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoWrapper {
    }

    private final XmlMapper VANILLA_MAPPER = newMapper();

    public void testNoWrapper() throws Exception {
        Foo foo = new Foo(Arrays.asList("Value1", "Value2"));

        assertEquals("<Foo><bar><bar>Value1</bar><bar>Value2</bar></bar></Foo>",
                VANILLA_MAPPER.writeValueAsString(foo));

        XmlMapper customMapper = mapperBuilder()
            .addModule(new SimpleModule("NoWrapperModule") {
                private static final long serialVersionUID = 1L;

                @Override
                public void setupModule(SetupContext context) {
                    context.insertAnnotationIntrospector(new NoWrapperIntrospector());
                    super.setupModule(context);
                }
            }).build();

        // After fixing https://github.com/FasterXML/jackson-databind/issues/4595
        assertEquals("<Foo><bar>Value1</bar><bar>Value2</bar></Foo>", customMapper.writeValueAsString(foo));

        // before fix had:
        //assertEquals("<Foo><bar><bar>Value1</bar><bar>Value2</bar></bar></Foo>", customMapper.writeValueAsString(foo));
    }
}
