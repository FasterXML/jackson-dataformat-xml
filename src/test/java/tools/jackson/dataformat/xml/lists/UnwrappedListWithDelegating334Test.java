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

    private final XmlMapper MAPPER = XmlMapper.builder()
            .addModule(new SimpleModule("test")
                    .setDeserializerModifier(new WrappingModifier()))
            .build();

    @Test
    public void testUnwrappedListWithDelegatingDeserializer() throws Exception
    {
        String xml = "<batch>"
                + "<message><text>one</text></message>"
                + "<message><text>two</text></message>"
                + "</batch>";
        Batch batch = MAPPER.readValue(xml, Batch.class);
        assertNotNull(batch);
        assertNotNull(batch.messages);
        assertEquals(2, batch.messages.size());
        assertEquals("one", batch.messages.get(0).text);
        assertEquals("two", batch.messages.get(1).text);
    }
}
