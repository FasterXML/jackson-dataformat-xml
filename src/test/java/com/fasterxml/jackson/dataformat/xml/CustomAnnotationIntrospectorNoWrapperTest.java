package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * A regression test for https://github.com/FasterXML/jackson-databind/issues/4595
 */
public class CustomAnnotationIntrospectorNoWrapperTest extends XmlTestBase {
    private final XmlMapper MAPPER = newMapper();

    public void testNoWrapper() throws Exception {
        Foo foo = new Foo(Arrays.asList("Value1", "Value2"));

        assertEquals("<Foo><bar><bar>Value1</bar><bar>Value2</bar></bar></Foo>", MAPPER.writeValueAsString(foo));

        MAPPER
            .registerModule(new SimpleModule("NoWrapperModule") {
                @Override
                public void setupModule(SetupContext context) {
                    context.insertAnnotationIntrospector(new NoWrapperIntrospector());
                    super.setupModule(context);
                }
            });

        // TODO: update after fixing https://github.com/FasterXML/jackson-databind/issues/4595
        // Should be assertEquals("<Foo><bar>Value1</bar><bar>Value2</bar></Foo>", MAPPER.writeValueAsString(foo));
        assertEquals("<Foo><bar><bar>Value1</bar><bar>Value2</bar></bar></Foo>", MAPPER.writeValueAsString(foo));
    }

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

    public static class NoWrapperIntrospector extends AnnotationIntrospector {
        @Override
        public Version version() {
            return com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION;
        }

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
}
