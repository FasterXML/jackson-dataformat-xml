package tools.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for [dataformat-xml#344]: Unable to deserialize null in polymorphic Collection.
 *<p>
 * With Jackson 3.x default of {@code WRITE_NULLS_AS_XSI_NIL = true},
 * null collection elements serialize as {@code <details xsi:nil="true"/>}.
 * The deserializer must recognize xsi:nil on array elements and produce
 * {@code VALUE_NULL} instead of {@code START_OBJECT}/{@code END_OBJECT}.
 */
public class PolymorphicListNullElement344Test extends XmlTestUtil
{
    static class Master344 {
        public List<Detail344> details = new ArrayList<>();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "cl")
    static class Detail344 {
        public String value;

        protected Detail344() { }
        public Detail344(String v) { value = v; }
    }

    private final XmlMapper MAPPER = newMapper();
    
    // [dataformat-xml#344]: null element in polymorphic list, round-trip
    @Test
    public void testNullInPolymorphicListRoundTrip() throws Exception
    {
        Master344 master = new Master344();
        master.details.add(new Detail344("first"));
        master.details.add(null);
        master.details.add(new Detail344("third"));

        String xml = MAPPER.writeValueAsString(master);

        Master344 result = MAPPER.readValue(xml, Master344.class);

        assertNotNull(result);
        assertNotNull(result.details);
        assertEquals(3, result.details.size());
        assertNotNull(result.details.get(0));
        assertEquals("first", result.details.get(0).value);
        assertNull(result.details.get(1));
        assertNotNull(result.details.get(2));
        assertEquals("third", result.details.get(2).value);
    }

    // [dataformat-xml#344]: null element in polymorphic list, deserialize from xsi:nil XML
    @Test
    public void testNullInPolymorphicListFromXsiNil() throws Exception
    {
        String xml = "<Master344>"
                + "<details>"
                + "<details xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "</details>"
                + "</Master344>";

        Master344 result = MAPPER.readValue(xml, Master344.class);

        assertNotNull(result);
        assertNotNull(result.details);
        assertEquals(1, result.details.size());
        assertNull(result.details.get(0));
    }

    // [dataformat-xml#344]: only null elements in polymorphic list
    @Test
    public void testOnlyNullsInPolymorphicList() throws Exception
    {
        Master344 master = new Master344();
        master.details.add(null);

        String xml = MAPPER.writeValueAsString(master);
        Master344 result = MAPPER.readValue(xml, Master344.class);

        assertNotNull(result);
        assertNotNull(result.details);
        assertEquals(1, result.details.size());
        assertNull(result.details.get(0));
    }

    // [dataformat-xml#344]: multiple consecutive nulls in polymorphic list.
    // First null exercises _mayBeLeaf + END_ELEMENT path (fix site 1+3),
    // second null exercises inArray() path (fix site 2).
    @Test
    public void testMultipleNullsInPolymorphicList() throws Exception
    {
        Master344 master = new Master344();
        master.details.add(null);
        master.details.add(null);
        master.details.add(new Detail344("third"));

        String xml = MAPPER.writeValueAsString(master);
        Master344 result = MAPPER.readValue(xml, Master344.class);

        assertNotNull(result);
        assertNotNull(result.details);
        assertEquals(3, result.details.size());
        assertNull(result.details.get(0));
        assertNull(result.details.get(1));
        assertNotNull(result.details.get(2));
        assertEquals("third", result.details.get(2).value);
    }
}
