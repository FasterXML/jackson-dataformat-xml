package tools.jackson.dataformat.xml.ser;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

// [dataformat-xml#556]: nested arrays/Collections cannot be represented in
// natural-style XML without an intermediate POJO; XmlMapper currently fails
// fast on serialization rather than silently flattening dimensions.
public class MultidimArray556Test extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    // Documents the current fail-fast behavior: 2D primitive array throws on
    // serialization (was silently producing the same output as 1D before fix).
    @Test
    public void test2DPrimitiveArrayFailsFast() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new boolean[][] { { true }, { false } });
            fail("Should not pass: nested arrays must be rejected");
        } catch (JacksonException e) {
            verifyException(e, "does not support nested arrays");
        }
    }

    // Same for nested Lists.
    @Test
    public void testNestedListFailsFast() throws Exception
    {
        List<List<String>> nested = Arrays.asList(
                Arrays.asList("a", "b"), Arrays.asList("c"));
        try {
            MAPPER.writeValueAsString(nested);
            fail("Should not pass: nested Collections must be rejected");
        } catch (JacksonException e) {
            verifyException(e, "does not support nested arrays");
        }
    }

    // Disabling FAIL_ON_NESTED_ARRAYS restores legacy 2.x behavior: nested
    // dimensions are silently flattened (no exception thrown).
    @Test
    public void testLegacyFlatteningWhenFeatureDisabled() throws Exception
    {
        String xml = MAPPER.writer()
                .without(XmlWriteFeature.FAIL_ON_NESTED_ARRAYS)
                .writeValueAsString(new boolean[][] { { true }, { false } });
        assertEquals("<booleans><item>true</item><item>false</item></booleans>", xml);
    }

    // Eventual goal: a 2D array should round-trip with proper nesting.
    // Currently fails (fail-fast above); annotation inverts pass/fail so this
    // entry tracks the unsupported-but-desired behavior.
    /*
    @JacksonTestFailureExpected
    @Test
    public void test2DArrayRoundTrip() throws Exception
    {
        boolean[][] input = new boolean[][] { { true }, { false } };
        String xml = MAPPER.writeValueAsString(input);
        boolean[][] result = MAPPER.readValue(xml, boolean[][].class);
        assertEquals(input.length, result.length);
        assertEquals(input[0][0], result[0][0]);
        assertEquals(input[1][0], result[1][0]);
    }
    */
}
