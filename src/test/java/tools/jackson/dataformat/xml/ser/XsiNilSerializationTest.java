package tools.jackson.dataformat.xml.ser;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#360]
public class XsiNilSerializationTest extends XmlTestUtil
{
    static class WrapperBean<T>
    {
        public T value;

        public WrapperBean() { }
        public WrapperBean(T v) { value = v; }
    }

    private final XmlMapper MAPPER = XmlMapper.builder()
            .configure(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL, true)
            .build();
    
    // [dataformat-xml#360]
    @Test
    public void testNilPropertyNoIndent() throws IOException
    {
        assertEquals("<WrapperBean><value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></WrapperBean>",
                MAPPER.writeValueAsString(new WrapperBean<>(null)));
    }

    // [dataformat-xml#360]
    @Test
    public void testNilPropertyRoot() throws IOException
    {
        // Not sure root element name defined but... "<null>" is what it is :)
        assertEquals("<null xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>",
                MAPPER.writeValueAsString(null));
    }

    // [dataformat-xml#432]
    @Test
    public void testNilPropertyWithIndent() throws IOException
    {
        final String xml = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(new WrapperBean<>(null))
                .trim();
        assertEquals("<WrapperBean>" + DEFAULT_NEW_LINE
            + "  <value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>" + DEFAULT_NEW_LINE
            + "</WrapperBean>", xml);
    }
}
