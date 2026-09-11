package tools.jackson.dataformat.xml.deser.builder;


import org.junit.jupiter.api.Test;


import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

public class BuilderWithTextValueTest extends XmlTestUtil
{
    @JsonDeserialize(builder = Outer.OuterBuilder.class)
    public static class Outer {

        final Inner inner;

        final String title;

        private Outer(Inner inner, String title) {
            this.inner = inner;
            this.title = title;
        }

        public static OuterBuilder builder() {
            return new OuterBuilder();
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class OuterBuilder {
            private Inner inner;
            private String title;

            @JacksonXmlProperty(localName = "Inner")
            public OuterBuilder inner(Inner inner) {
                this.inner = inner;
                return this;
            }

            @JacksonXmlProperty(localName = "Title")
            public OuterBuilder title(String title) {
                this.title = title;
                return this;
            }

            public Outer build() {
                return new Outer(inner, title);
            }
        }
    }

    @JsonDeserialize(builder = Inner.InnerBuilder.class)
    public static class Inner {

        final String value;

        final String title;

        private Inner(String value, String title) {
            this.value = value;
            this.title = title;
        }

        public static InnerBuilder builder() {
            return new InnerBuilder();
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class InnerBuilder {
            private String value;
            private String title;

            @JacksonXmlText
            public InnerBuilder value(String value) {
                this.value = value;
                return this;
            }

            @JacksonXmlProperty(isAttribute = true)
            public InnerBuilder title(String title) {
                this.title = title;
                return this;
            }

            public Inner build() {
                return new Inner(value, title);
            }
        }
    }

    @Test
    public void testNestedBuilderWithTextValue() throws Exception
    {
        XmlMapper mapper = XmlMapper.builder().build();
        final String XML = "<Outer><Inner>Value</Inner></Outer>";
        Outer outer = mapper.readValue(XML, Outer.class);
        assertEquals("Value", outer.inner.value);
    }
}
