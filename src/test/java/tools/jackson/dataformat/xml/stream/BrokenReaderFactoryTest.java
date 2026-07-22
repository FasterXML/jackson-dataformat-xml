package tools.jackson.dataformat.xml.stream;

import java.io.*;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.junit.jupiter.api.Test;

import com.ctc.wstx.stax.WstxInputFactory;

import tools.jackson.core.exc.StreamReadException;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertThrows;

// [dataformat-xml#618]: some non-Woodstox Stax impls (notably JDK-bundled SJSXP)
// parse the XML declaration while creating the stream reader and fail with unchecked
// exceptions on malformed input. XmlFactory already translates the reader.next() and
// _initializeXmlReader cases; this checks the createXMLStreamReader boundary too, so
// callers see a StreamReadException rather than a leaked ArrayIndexOutOfBoundsException.
public class BrokenReaderFactoryTest extends XmlTestUtil
{
    // Stands in for a non-Woodstox impl whose reader creation throws non-XMLStreamException
    static class BrokenInputFactory extends WstxInputFactory {
        @Override
        public XMLStreamReader createXMLStreamReader(InputStream in) {
            throw new ArrayIndexOutOfBoundsException("Index 5 out of bounds for length 4");
        }

        @Override
        public XMLStreamReader createXMLStreamReader(Reader r) {
            throw new ArrayIndexOutOfBoundsException("Index 5 out of bounds for length 4");
        }

        @Override
        public XMLStreamReader createXMLStreamReader(Source s) {
            throw new ArrayIndexOutOfBoundsException("Index 5 out of bounds for length 4");
        }
    }

    private final XmlMapper MAPPER = new XmlMapper(new XmlFactory(new BrokenInputFactory()));

    private static final String DOC = "<root>value</root>";

    @Test
    public void testByteArrayInput() throws Exception {
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> MAPPER.readTree(utf8Bytes(DOC)));
        verifyException(e, "Internal processing error by `XMLInputFactory`");
    }

    @Test
    public void testInputStreamInput() throws Exception {
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> MAPPER.readTree(new ByteArrayInputStream(utf8Bytes(DOC))));
        verifyException(e, "Internal processing error by `XMLInputFactory`");
    }

    @Test
    public void testReaderInput() throws Exception {
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> MAPPER.readTree(new StringReader(DOC)));
        verifyException(e, "Internal processing error by `XMLInputFactory`");
    }

    @Test
    public void testStringInput() throws Exception {
        StreamReadException e = assertThrows(StreamReadException.class,
                () -> MAPPER.readTree(DOC));
        verifyException(e, "Internal processing error by `XMLInputFactory`");
    }
}
