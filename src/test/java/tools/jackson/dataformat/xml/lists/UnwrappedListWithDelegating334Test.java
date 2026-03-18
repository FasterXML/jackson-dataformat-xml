package tools.jackson.dataformat.xml.lists;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.*;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for [dataformat-xml#334]: {@code @JacksonXmlElementWrapper(useWrapping = false)}
 * should work even when a user-provided {@link ValueDeserializerModifier} wraps the
 * bean deserializer in a {@link DelegatingDeserializer}.
 */
public class UnwrappedListWithDelegating334Test extends XmlTestUtil
{
    @JsonRootName("batch")
    static class Batch {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "message")
        public List<Message> messages;
    }

    static class Message {
        @JacksonXmlProperty(localName = "text")
        public String text;
    }

    // Minimal DelegatingDeserializer that just passes through
    static class PassthroughDeserializer extends DelegatingDeserializer {
        public PassthroughDeserializer(ValueDeserializer<?> delegate) {
            super(delegate);
        }

        @Override
        protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee) {
            return new PassthroughDeserializer(newDelegatee);
        }
    }

    // Modifier that wraps every deserializer in a PassthroughDeserializer
    static class WrappingModifier extends ValueDeserializerModifier {
        private static final long serialVersionUID = 1L;

        @Override
        public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config,
                BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
            return new PassthroughDeserializer(deserializer);
        }
    }

    // Another passthrough, to test multi-level delegation chains
    static class AnotherPassthroughDeserializer extends DelegatingDeserializer {
        public AnotherPassthroughDeserializer(ValueDeserializer<?> delegate) {
            super(delegate);
        }

        @Override
        protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee) {
            return new AnotherPassthroughDeserializer(newDelegatee);
        }
    }

    // Modifier that double-wraps: Passthrough -> AnotherPassthrough -> original
    static class DoubleWrappingModifier extends ValueDeserializerModifier {
        private static final long serialVersionUID = 1L;

        @Override
        public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config,
                BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
            return new PassthroughDeserializer(
                    new AnotherPassthroughDeserializer(deserializer));
        }
    }

    private final XmlMapper MAPPER = XmlMapper.builder()
            .addModule(new SimpleModule("test")
                    .setDeserializerModifier(new WrappingModifier()))
            .build();

    private final XmlMapper MAPPER_DOUBLE = XmlMapper.builder()
            .addModule(new SimpleModule("test")
                    .setDeserializerModifier(new DoubleWrappingModifier()))
            .build();

    private final String BATCH_XML = "<batch>"
            + "<message><text>one</text></message>"
            + "<message><text>two</text></message>"
            + "</batch>";

    @Test
    public void testUnwrappedListWithDelegatingDeserializer() throws Exception
    {
        Batch batch = MAPPER.readValue(BATCH_XML, Batch.class);
        assertNotNull(batch);
        assertNotNull(batch.messages);
        assertEquals(2, batch.messages.size());
        assertEquals("one", batch.messages.get(0).text);
        assertEquals("two", batch.messages.get(1).text);
    }

    @Test
    public void testUnwrappedListWithMultiLevelDelegation() throws Exception
    {
        Batch batch = MAPPER_DOUBLE.readValue(BATCH_XML, Batch.class);
        assertNotNull(batch);
        assertNotNull(batch.messages);
        assertEquals(2, batch.messages.size());
        assertEquals("one", batch.messages.get(0).text);
        assertEquals("two", batch.messages.get(1).text);
    }
}
