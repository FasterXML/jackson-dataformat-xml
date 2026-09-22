package tools.jackson.dataformat.xml.misc;

import java.io.*;

import org.junit.jupiter.api.Test;

import tools.jackson.core.ErrorReportConfiguration;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.core.exc.StreamReadException;

import tools.jackson.databind.JsonNode;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Settings of `XmlFactory` other than format features must survive JDK serialization
public class FactoryConfigAfterSerializationTest extends XmlTestUtil
{
    private final static String NESTED_XML = "<a><b><c><d><e>1</e></d></c></b></a>";

    private final static String DUP_XML = "<root><a>1</a><b>2</b><a>3</a></root>";

    private XmlFactory factoryWithLimits() {
        return XmlFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNestingDepth(3)
                        .maxStringLength(1234)
                        .build())
                .streamWriteConstraints(StreamWriteConstraints.builder()
                        .maxNestingDepth(7)
                        .build())
                .errorReportConfiguration(ErrorReportConfiguration.builder()
                        .maxErrorTokenLength(17)
                        .build())
                .build();
    }

    @Test
    public void testConstraintsRetainedForFactory() throws Exception
    {
        XmlFactory f = jdkRoundtrip(factoryWithLimits());
        assertEquals(3, f.streamReadConstraints().getMaxNestingDepth());
        assertEquals(1234, f.streamReadConstraints().getMaxStringLength());
        assertEquals(7, f.streamWriteConstraints().getMaxNestingDepth());
        assertEquals(17, f.errorReportConfiguration().getMaxErrorTokenLength());
    }

    @Test
    public void testConstraintsRetainedForMapper() throws Exception
    {
        XmlMapper mapper = new XmlMapper(factoryWithLimits());
        // sanity check: limit in effect to begin with
        assertThrows(StreamConstraintsException.class,
                () -> mapper.readTree(NESTED_XML));

        XmlMapper mapper2 = jdkRoundtrip(mapper);
        StreamConstraintsException e = assertThrows(StreamConstraintsException.class,
                () -> mapper2.readTree(NESTED_XML));
        verifyException(e, "Document nesting depth", "exceeds the maximum allowed");
    }

    @Test
    public void testStreamFeaturesRetained() throws Exception
    {
        XmlFactory f = XmlFactory.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
                .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                .build();
        XmlFactory f2 = jdkRoundtrip(f);
        assertTrue(f2.isEnabled(StreamReadFeature.STRICT_DUPLICATE_DETECTION));
        assertFalse(f2.isEnabled(StreamReadFeature.AUTO_CLOSE_SOURCE));
        assertFalse(f2.isEnabled(StreamWriteFeature.AUTO_CLOSE_TARGET));

        XmlMapper mapper2 = jdkRoundtrip(new XmlMapper(f));
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> mapper2.readValue(DUP_XML, JsonNode.class));
        verifyException(e, "Duplicate Object property \"a\"");
    }

    @SuppressWarnings("unchecked")
    private <T> T jdkRoundtrip(T value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bytes)) {
            os.writeObject(value);
        }
        try (ObjectInputStream is = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) is.readObject();
        }
    }
}
