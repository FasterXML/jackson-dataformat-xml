package tools.jackson.dataformat.xml.ser;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * See <a href="https://github.com/FasterXML/jackson-dataformat-xml/issues/270">issue #270</a>
 * for details
 */
public class TestBinaryStreamToXMLSerialization extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testWith0Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo());
        assertEquals("<TestPojo><field/></TestPojo>", xml);
    }

    @Test
    public void testWith1Byte() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A' ));
        assertEquals("<TestPojo><field>QQ==</field></TestPojo>", xml);
    }

    @Test
    public void testWith2Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B' ));
        assertEquals("<TestPojo><field>QUI=</field></TestPojo>", xml);
    }

    @Test
    public void testWith3Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C' ));
        assertEquals("<TestPojo><field>QUJD</field></TestPojo>", xml);
    }

    @Test
    public void testWith4Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C', 'D' ));
        assertEquals("<TestPojo><field>QUJDRA==</field></TestPojo>", xml);
    }

    private TestPojo createPojo(char... content) {
        TestPojo obj = new TestPojo();
        // DirectByteBuffer does not have an underlying array
        // so the ByteArraySerializer has to fallback to stream writing
        obj.field = ByteBuffer.allocateDirect(content.length);
        for(char b : content) {
            obj.field.put((byte) b);
        }
        obj.field.position(0);
        return obj;
    }

    public static class TestPojo {
        public ByteBuffer field;
    }
} 
