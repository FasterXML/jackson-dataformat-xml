package tools.jackson.dataformat.xml.records;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#884]: NPE when Record has `@JsonIgnore`d getter (which leaves
// behind a property definition with no accessors at all)
public class RecordIgnoredGetter884Test extends XmlTestUtil
{
    public interface Id884 {
        String name();

        @JsonIgnore
        default String getQualifiedName() {
            return "id:" + name();
        }
    }

    public record ObjectId884(String name, String repo) implements Id884 { }

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testIgnoredDefaultGetter() throws Exception {
        ObjectId884 input = new ObjectId884("value", "repo");
        String xml = MAPPER.writeValueAsString(input);
        assertEquals(input, MAPPER.readValue(xml, ObjectId884.class));
    }
}
