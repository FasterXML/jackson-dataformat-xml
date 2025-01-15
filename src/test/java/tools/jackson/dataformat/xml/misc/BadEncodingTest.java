package tools.jackson.dataformat.xml.misc;

import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// [dataformat-xml#428]: problem with an encoding supported via JDK
public class BadEncodingTest extends XmlTestUtil
{
//    private static final String xml = "<?xml version='1.0' encoding='WINDOWS-1252'?><x/>";
    private static final String xml = "<?xml version='1.0' encoding='WINDOWS-1252'?><x/>";

    private final ObjectMapper XML_MAPPER = newMapper();

    @Test
    public void testEncoding() throws Exception {
        final byte[] b = xml.getBytes("UTF-8");
        assertNotNull(XML_MAPPER.readValue(b, Map.class));

//        try (InputStream in = new ByteArrayInputStream(b)) {
//            XML_MAPPER.readValue(in, Map.class);
//        }
    }
}
