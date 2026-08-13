package tools.jackson.dataformat.xml.dos;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.core.StreamWriteConstraints;

import tools.jackson.databind.DatabindException;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Simple unit tests to verify that we fail gracefully if you attempt to serialize
 * data that is cyclic (eg a map that contains itself).
 */
public class CyclicXMLDataSerTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testMapWithSelfReference() throws Exception {
        // Use Maps (rather than Lists) so the cycle exercises object-nesting
        // depth without tripping XmlWriteFeature.FAIL_ON_NESTED_ARRAYS.
        // Two distinct Maps avoid trivial self-loop detection.
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        map1.put("ref", map2);
        map2.put("ref", map1);
        try {
            MAPPER.writeValueAsString(map1);
            fail("expected DatabindException for infinite recursion");
        } catch (DatabindException e) {
            String exceptionPrefix = String.format("Document nesting depth (%d) exceeds the maximum allowed",
                    StreamWriteConstraints.DEFAULT_MAX_DEPTH + 1);
            assertTrue(e.getMessage().startsWith(exceptionPrefix),
                    "Unexpected message: " + e.getMessage());
        }
    }
}
