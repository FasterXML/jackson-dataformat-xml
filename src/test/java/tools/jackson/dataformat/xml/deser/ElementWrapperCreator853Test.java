package tools.jackson.dataformat.xml.deser;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#853] InvalidDefinitionException on classes using
// @JacksonXmlElementWrapper and @JacksonXmlProperty on a field when class
// has a matching args-constructor (e.g., record).
public class ElementWrapperCreator853Test extends XmlTestUtil
{
    // Class version: field-annotated only, no @JsonCreator, single args ctor
    static class GroupDto {
        @JacksonXmlElementWrapper(localName = "users")
        @JacksonXmlProperty(localName = "user")
        private List<String> users;

        private String name;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public GroupDto(@JsonProperty("users") List<String> users,
                @JsonProperty("name") String name) {
            this.users = users;
            this.name = name;
        }

        public List<String> getUsers() { return users; }
        public String getName() { return name; }
    }

    // Record version: most direct form
    public record GroupRecord(
            @JacksonXmlElementWrapper(localName = "users")
            @JacksonXmlProperty(localName = "user")
            List<String> users,
            String name
    ) {}

    private static final String XML =
            "<GroupDto>"
            + "<users>"
            + "<user>A</user>"
            + "<user>B</user>"
            + "</users>"
            + "<name>g1</name>"
            + "</GroupDto>";

    @Test
    public void testDeserializeClass() throws Exception
    {
        GroupDto result = newMapper().readValue(XML, GroupDto.class);
        assertNotNull(result.getUsers());
        assertEquals(2, result.getUsers().size());
        assertEquals("A", result.getUsers().get(0));
        assertEquals("B", result.getUsers().get(1));
        assertEquals("g1", result.getName());
    }

    @Test
    public void testDeserializeRecord() throws Exception
    {
        String xml = XML.replace("GroupDto", "GroupRecord");
        GroupRecord result = newMapper().readValue(xml, GroupRecord.class);
        assertNotNull(result.users());
        assertEquals(2, result.users().size());
        assertEquals("A", result.users().get(0));
        assertEquals("B", result.users().get(1));
        assertEquals("g1", result.name());
    }
}
