package tools.jackson.dataformat.xml.stream;

import java.io.*;

import javax.xml.stream.*;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.ctc.wstx.stax.WstxInputFactory;

import tools.jackson.core.exc.StreamReadException;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertThrows;

// [dataformat-xml#883]: `XmlFactory` must translate unchecked failures of
// `XMLInputFactory.createXMLStreamReader()` into `StreamReadException`, so that callers
// only ever need to catch `JacksonException`. 2.x guards the `byte[]` + non-Stax2 case
// (added 04-Dec-2023 while working on [dataformat-xml#618]) but the check was lost in
// the 3.x port.
//
// Two stand-in factories are needed because `XmlFactory` branches on
// `instanceof XMLInputFactory2` for `byte[]`/`char[]` input: Stax2 impls get handed
// `Stax2ByteArraySource`/`Stax2CharArraySource`, plain ones a
// `ByteArrayInputStream`/`CharArrayReader` -- and it is the latter branch that
// JDK-bundled SJSXP was seen failing on.
public class BrokenReaderFactoryTest extends XmlTestUtil
{
    private final static String DOC = "<root>value</root>";

    /**
     * Stand-in for a Stax2-capable impl whose reader creation throws
     * non-{@code XMLStreamException}.
     */
    static class BrokenStax2InputFactory extends WstxInputFactory {
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

    /**
     * Stand-in for a plain (non-Stax2) impl, like JDK-bundled SJSXP: throws
     * {@link StringIndexOutOfBoundsException} so that widening of the guard beyond
     * {@code ArrayIndexOutOfBoundsException} gets covered too.
     */
    static class BrokenBasicInputFactory extends XMLInputFactory {
        private final XMLInputFactory _delegate = XMLInputFactory.newDefaultFactory();

        // The 3 creation methods `XmlFactory` may call: all fail

        @Override
        public XMLStreamReader createXMLStreamReader(Reader r) {
            throw new StringIndexOutOfBoundsException("begin 0, end 3, length 2");
        }

        @Override
        public XMLStreamReader createXMLStreamReader(InputStream in) {
            throw new StringIndexOutOfBoundsException("begin 0, end 3, length 2");
        }

        @Override
        public XMLStreamReader createXMLStreamReader(Source s) {
            throw new StringIndexOutOfBoundsException("begin 0, end 3, length 2");
        }

        // ... and plain delegation for the rest (`setProperty()` at least does get called)

        @Override
        public XMLStreamReader createXMLStreamReader(InputStream in, String enc) throws XMLStreamException {
            return _delegate.createXMLStreamReader(in, enc);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(String sysId, InputStream in) throws XMLStreamException {
            return _delegate.createXMLStreamReader(sysId, in);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(String sysId, Reader r) throws XMLStreamException {
            return _delegate.createXMLStreamReader(sysId, r);
        }

        @Override
        public XMLEventReader createXMLEventReader(Reader r) throws XMLStreamException {
            return _delegate.createXMLEventReader(r);
        }

        @Override
        public XMLEventReader createXMLEventReader(String sysId, Reader r) throws XMLStreamException {
            return _delegate.createXMLEventReader(sysId, r);
        }

        @Override
        public XMLEventReader createXMLEventReader(XMLStreamReader sr) throws XMLStreamException {
            return _delegate.createXMLEventReader(sr);
        }

        @Override
        public XMLEventReader createXMLEventReader(Source s) throws XMLStreamException {
            return _delegate.createXMLEventReader(s);
        }

        @Override
        public XMLEventReader createXMLEventReader(InputStream in) throws XMLStreamException {
            return _delegate.createXMLEventReader(in);
        }

        @Override
        public XMLEventReader createXMLEventReader(InputStream in, String enc) throws XMLStreamException {
            return _delegate.createXMLEventReader(in, enc);
        }

        @Override
        public XMLEventReader createXMLEventReader(String sysId, InputStream in) throws XMLStreamException {
            return _delegate.createXMLEventReader(sysId, in);
        }

        @Override
        public XMLStreamReader createFilteredReader(XMLStreamReader sr, StreamFilter f) throws XMLStreamException {
            return _delegate.createFilteredReader(sr, f);
        }

        @Override
        public XMLEventReader createFilteredReader(XMLEventReader er, EventFilter f) throws XMLStreamException {
            return _delegate.createFilteredReader(er, f);
        }

        @Override
        public XMLResolver getXMLResolver() { return _delegate.getXMLResolver(); }

        @Override
        public void setXMLResolver(XMLResolver r) { _delegate.setXMLResolver(r); }

        @Override
        public XMLReporter getXMLReporter() { return _delegate.getXMLReporter(); }

        @Override
        public void setXMLReporter(XMLReporter r) { _delegate.setXMLReporter(r); }

        @Override
        public void setProperty(String name, Object value) { _delegate.setProperty(name, value); }

        @Override
        public Object getProperty(String name) { return _delegate.getProperty(name); }

        @Override
        public boolean isPropertySupported(String name) { return _delegate.isPropertySupported(name); }

        @Override
        public void setEventAllocator(XMLEventAllocator a) { _delegate.setEventAllocator(a); }

        @Override
        public XMLEventAllocator getEventAllocator() { return _delegate.getEventAllocator(); }
    }

    private final XmlMapper STAX2_MAPPER = new XmlMapper(new XmlFactory(new BrokenStax2InputFactory()));

    private final XmlMapper BASIC_MAPPER = new XmlMapper(new XmlFactory(new BrokenBasicInputFactory()));

    /*
    /**********************************************************************
    /* Stax2-capable factory: `Stax2ByteArraySource`/`Stax2CharArraySource` branch
    /**********************************************************************
     */

    @Test
    public void testStax2ByteArrayInput() throws Exception {
        _verifyBadReaderCreation(() -> STAX2_MAPPER.readTree(utf8Bytes(DOC)));
    }

    @Test
    public void testStax2CharArrayInput() throws Exception {
        _verifyBadReaderCreation(() -> STAX2_MAPPER.createParser(DOC.toCharArray()));
    }

    @Test
    public void testStax2InputStreamInput() throws Exception {
        _verifyBadReaderCreation(() -> STAX2_MAPPER.readTree(new ByteArrayInputStream(utf8Bytes(DOC))));
    }

    @Test
    public void testStax2ReaderInput() throws Exception {
        _verifyBadReaderCreation(() -> STAX2_MAPPER.readTree(new StringReader(DOC)));
    }

    /*
    /**********************************************************************
    /* Plain factory: `ByteArrayInputStream`/`CharArrayReader` branch
    /**********************************************************************
     */

    @Test
    public void testBasicByteArrayInput() throws Exception {
        _verifyBadReaderCreation(() -> BASIC_MAPPER.readTree(utf8Bytes(DOC)));
    }

    @Test
    public void testBasicCharArrayInput() throws Exception {
        _verifyBadReaderCreation(() -> BASIC_MAPPER.createParser(DOC.toCharArray()));
    }

    private void _verifyBadReaderCreation(Executable exec) {
        StreamReadException e = assertThrows(StreamReadException.class, exec);
        verifyException(e, "Internal processing error by `XMLInputFactory`");
        verifyException(e, "when trying to create a parser");
    }
}
