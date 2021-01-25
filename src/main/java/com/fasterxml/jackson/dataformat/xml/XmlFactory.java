package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import javax.xml.stream.*;

import org.codehaus.stax2.io.Stax2ByteArraySource;
import org.codehaus.stax2.io.Stax2CharArraySource;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.TextualTSFactory;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonFactoryBuilder;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
* Factory used for constructing {@link FromXmlParser} and {@link ToXmlGenerator}
* instances.
*<p>
* Implements {@link TokenStreamFactory} since interface for constructing XML backed
* parsers and generators is quite similar to dealing with JSON.
* 
* @author Tatu Saloranta (tatu.saloranta@iki.fi)
*/
public class XmlFactory
    extends TextualTSFactory
    implements java.io.Serializable
{
    private static final long serialVersionUID = 2; // 3.0

    /**
     * Name used to identify XML format
     * (and returned by {@link #getFormatName()}
     */
    public final static String FORMAT_NAME_XML = "XML";

    /**
     * Bitfield (set of flags) of all parser features that are enabled
     * by default.
     */
    final static int DEFAULT_XML_PARSER_FEATURE_FLAGS = FromXmlParser.Feature.collectDefaults();

    /**
     * Bitfield (set of flags) of all generator features that are enabled
     * by default.
     */
    final static int DEFAULT_XML_GENERATOR_FEATURE_FLAGS = ToXmlGenerator.Feature.collectDefaults();

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    // !!! 09-Jan-2018, tatu: make final ASAP
    protected String _cfgNameForTextElement;

    // // Transient just because JDK serializability requires some trickery

    protected transient final XMLInputFactory _xmlInputFactory;

    protected transient final XMLOutputFactory _xmlOutputFactory;

    /*
    /**********************************************************************
    /* Factory construction, configuration
    /**********************************************************************
     */

    /**
     * Default constructor used to create factory instances.
     * Creation of a factory instance is a light-weight operation,
     * but it is still a good idea to reuse limited number of
     * factory instances (and quite often just a single instance):
     * factories are used as context for storing some reused
     * processing objects (such as symbol tables parsers use)
     * and this reuse only works within context of a single
     * factory instance.
     */
    public XmlFactory() { this(null, (XMLOutputFactory) null); }

    public XmlFactory(XMLInputFactory xmlIn) {
        this(xmlIn, null);
    }

    public XmlFactory(XMLInputFactory xmlIn, XMLOutputFactory xmlOut) {
        this(DEFAULT_XML_PARSER_FEATURE_FLAGS, DEFAULT_XML_GENERATOR_FEATURE_FLAGS,
                xmlIn, xmlOut, null);
    }

    protected XmlFactory(int xpFeatures, int xgFeatures,
            XMLInputFactory xmlIn, XMLOutputFactory xmlOut,
            String nameForTextElem)
    {
        super(xpFeatures, xgFeatures);
        _cfgNameForTextElement = nameForTextElem;
        if (xmlIn == null) {
            xmlIn = XmlFactoryBuilder.defaultInputFactory();
        }
        if (xmlOut == null) {
            xmlOut = XmlFactoryBuilder.defaultOutputFactory();
        }
        _initFactories(xmlIn, xmlOut);
        _xmlInputFactory = xmlIn;
        _xmlOutputFactory = xmlOut;
    }

    /**
     * Constructors used by {@link JsonFactoryBuilder} for instantiation.
     *
     * @since 3.0
     */
    protected XmlFactory(XmlFactoryBuilder b)
    {
        super(b);
        _cfgNameForTextElement = b.nameForTextElement();
        _xmlInputFactory = b.inputFactory();
        _xmlOutputFactory = b.outputFactory();
        _initFactories(_xmlInputFactory, _xmlOutputFactory);
    }

    protected XmlFactory(XmlFactory src)
    {
        this(src, src._cfgNameForTextElement);
    }

    protected XmlFactory(XmlFactory src, String nameForTextElement)
    {
        super(src);
        _cfgNameForTextElement = nameForTextElement;
        _xmlInputFactory = src._xmlInputFactory;
        _xmlOutputFactory = src._xmlOutputFactory;
    }
    
    protected void _initFactories(XMLInputFactory xmlIn, XMLOutputFactory xmlOut)
    {
        // [dataformat-xml#326]: Better ensure namespaces get built properly, so:
        xmlOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        // and for parser, force coalescing as well (much simpler to use)
        xmlIn.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    }

    @Override
    public XmlFactoryBuilder rebuild() {
        return new XmlFactoryBuilder(this);
    }

    /**
     * Main factory method to use for constructing {@link XmlFactory} instances with
     * different configuration.
     */
    public static XmlFactoryBuilder builder() {
        return new XmlFactoryBuilder();
    }

    /**
     * Note: compared to base implementation by {@link TokenStreamFactory},
     * here the copy will actually share underlying XML input and
     * output factories, as there is no way to make copies of those.
     */
    @Override
    public XmlFactory copy() {
        return new XmlFactory(this);
    }

    /**
     * Instances are immutable so just return `this`
     */
    @Override
    public TokenStreamFactory snapshot() {
        return this;
    }

    public XmlFactory withNameForTextElement(String name) {
        if (name == null) {
            name = "";
        }
        if (name.equals(_cfgNameForTextElement)) {
            return this;
        }
        return new XmlFactory(this, name);
    }

    /*
    /**********************************************************************
    /* Serializable overrides
    /**********************************************************************
     */

    // Hiding place for JDK-serialization unthawed factories...
    protected transient String _jdkXmlInFactory;

    // Hiding place for JDK-serialization unthawed factories...
    protected transient String _jdkXmlOutFactory;

    /**
     * Method that we need to override to actually make restoration go
     * through constructors etc.
     */
    protected Object readResolve() {
        if (_jdkXmlInFactory == null) {
            throw new IllegalStateException("No XMLInputFactory class name read during JDK deserialization");
        }
        if (_jdkXmlOutFactory == null) {
            throw new IllegalStateException("No XMLOutputFactory class name read during JDK deserialization");
        }
        final XMLInputFactory inf;
        final XMLOutputFactory outf;
        try {
            inf = (XMLInputFactory) Class.forName(_jdkXmlInFactory).getDeclaredConstructor().newInstance();
            outf = (XMLOutputFactory) Class.forName(_jdkXmlOutFactory).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return new XmlFactory(_formatReadFeatures, _formatWriteFeatures,
                inf, outf, _cfgNameForTextElement);
    }

    /**
     * In addition to default serialization, which mostly works, need
     * to handle case of XML factories, hence override.
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _jdkXmlInFactory = in.readUTF();
        _jdkXmlOutFactory = in.readUTF();
    }

    /**
     * In addition to default serialization, which mostly works, need
     * to handle case of XML factories, hence override.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(_xmlInputFactory.getClass().getName());
        out.writeUTF(_xmlOutputFactory.getClass().getName());
    }

    /*
    /**********************************************************************
    /* Introspection: version, capabilities
    /**********************************************************************
     */

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public boolean canParseAsync() {
        return false;
    }

    /**
     * Since 2.4, we do have actual capability for passing char arrays
     * efficiently, but unfortunately
     * have no working mechanism for recycling buffers. So we have to 
     * admit that can not make efficient use.
     */
    @Override
    public boolean canUseCharArrays() { return false; }

    /*
    /**********************************************************************
    /* Format support
    /**********************************************************************
     */

    /**
     * Method that returns short textual id identifying format
     * this factory supports.
     *<p>
     * Note: sub-classes should override this method; default
     * implementation will return null for all sub-classes
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_XML;
    }

    @Override
    public boolean canUseSchema(FormatSchema schema) {
        return false; // no FormatSchema for xml
    }

    @Override
    public Class<FromXmlParser.Feature> getFormatReadFeatureType() {
        return FromXmlParser.Feature.class;
    }

    @Override
    public Class<ToXmlGenerator.Feature> getFormatWriteFeatureType() {
        return ToXmlGenerator.Feature.class;
    }

    @Override
    public int getFormatReadFeatures() { return _formatReadFeatures; }

    @Override
    public int getFormatWriteFeatures() { return _formatWriteFeatures; }
    
    /*
    /**********************************************************************
    /* Configuration, XML-specific
    /**********************************************************************
     */

    @Deprecated
    public void setXMLTextElementName(String name) {
        _cfgNameForTextElement = name;
    }

    public String getXMLTextElementName() {
        return _cfgNameForTextElement;
    }

    public XMLInputFactory getXMLInputFactory() {
        return _xmlInputFactory;
    }

    public XMLOutputFactory getXMLOutputFactory() {
        return _xmlOutputFactory;
    }

    /*
    /**********************************************************************
    /* Overrides of public methods: parsing
    /**********************************************************************
     */

    /**
     * Overridden just to prevent trying to optimize access via char array;
     * while nice idea, problem is that we don't have proper hooks to ensure
     * that temporary buffer gets recycled; so let's just use StringReader.
     */
    @Override
    public JsonParser createParser(ObjectReadContext readCtxt, String content) {
        Reader r = new StringReader(content);
        IOContext ioCtxt = _createContext(r, true);
        return _createParser(readCtxt, ioCtxt, _decorate(ioCtxt, r));
    }

    @Override
    protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, DataInput input)
    {
        return _unsupported();
    }

    /*
    /**********************************************************************
    /* Overrides of public methods: generation
    /**********************************************************************
     */

    @Override
    protected JsonGenerator _createGenerator(ObjectWriteContext writeCtxt,
            IOContext ioCtxt, Writer out)
    {
        // Only care about features and pretty-printer, for now;
        // may add CharacterEscapes in future?
        
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getStreamWriteFeatures(_streamWriteFeatures),
                writeCtxt.getFormatWriteFeatures(_formatWriteFeatures),
                _createXmlWriter(out),
                _xmlPrettyPrinter(writeCtxt));
    }

    @Override
    protected JsonGenerator _createUTF8Generator(ObjectWriteContext writeCtxt,
            IOContext ioCtxt, OutputStream out)
    {
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getStreamWriteFeatures(_streamWriteFeatures),
                writeCtxt.getFormatWriteFeatures(_formatWriteFeatures),
                _createXmlWriter(out),
                _xmlPrettyPrinter(writeCtxt));
    }

    private final XmlPrettyPrinter _xmlPrettyPrinter(ObjectWriteContext writeCtxt)
    {
        PrettyPrinter pp = writeCtxt.getPrettyPrinter();
        if (pp == null) {
            return null;
        }
        // Ideally should catch earlier, but just in case....
        if (!(pp instanceof XmlPrettyPrinter)) {
            throw new IllegalStateException("Configured PrettyPrinter not of type `XmlPrettyPrinter` but `"
                    +pp.getClass().getName()+"`");
        }
        return (XmlPrettyPrinter) pp;
    }

    /*
    /**********************************************************************
    /* Extended public API, mostly for XmlMapper
    /**********************************************************************
     */

    /**
     * Factory method that wraps given {@link XMLStreamReader}, usually to allow
     * partial data-binding.
     */
    public FromXmlParser createParser(ObjectReadContext readCtxt,
            XMLStreamReader sr) throws IOException
    {
        // note: should NOT move parser if already pointing to START_ELEMENT
        if (sr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            sr = _initializeXmlReader(sr);
        }

        // false -> not managed
        FromXmlParser xp = new FromXmlParser(readCtxt, _createContext(sr, false),
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    /**
     * Factory method that wraps given {@link XMLStreamWriter}, usually to allow
     * incremental serialization to compose large output by serializing a sequence
     * of individual objects.
     */
    public ToXmlGenerator createGenerator(ObjectWriteContext writeCtxt,
            XMLStreamWriter sw) throws IOException
    {
        sw = _initializeXmlWriter(sw);
        IOContext ioCtxt = _createContext(sw, false);
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getStreamWriteFeatures(_streamWriteFeatures),
                writeCtxt.getFormatWriteFeatures(_formatWriteFeatures),
                sw,
                _xmlPrettyPrinter(writeCtxt));
    }

    /*
    /**********************************************************************
    /* Internal factory method overrides
    /**********************************************************************
     */

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            InputStream in)
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            Reader r)
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(r);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            char[] data, int offset, int len,
            boolean recycleBuffer)
    {
        // !!! TODO: add proper handling of 'recycleBuffer'; currently its handling
        //    is always same as if 'false' was passed
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(new Stax2CharArraySource(data, offset, len));
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            byte[] data, int offset, int len)
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(new Stax2ByteArraySource(data, offset, len));
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    /*
    /**********************************************************************
    /* Internal factory methods, XML-specific
    /**********************************************************************
     */

    protected XMLStreamWriter _createXmlWriter(OutputStream out)
    {
        XMLStreamWriter sw;
        try {
            sw = _xmlOutputFactory.createXMLStreamWriter(out, "UTF-8");
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsWriteException(e, null);
        }
        return _initializeXmlWriter(sw);
    }

    protected XMLStreamWriter _createXmlWriter(Writer w)
    {
        XMLStreamWriter sw;
        try {
            sw = _xmlOutputFactory.createXMLStreamWriter(w);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsWriteException(e, null);
        }
        return _initializeXmlWriter(sw);
    }

    protected final XMLStreamWriter _initializeXmlWriter(XMLStreamWriter sw)
    {
        // And just for Sun Stax parser (JDK default), seems that we better define default namespace
        // (Woodstox doesn't care) -- otherwise it'll add unnecessary odd declaration
        try {
            sw.setDefaultNamespace("");
        } catch (Exception e) {
            throw new StreamWriteException(null, e.getMessage(), e);
        }
        return sw;
    }

    protected final XMLStreamReader _initializeXmlReader(XMLStreamReader sr)
    {
        try {
            // for now, nothing to do... except let's find the root element
            while (sr.next() != XMLStreamConstants.START_ELEMENT) {
                ;
            }
        // [dataformat-xml#350]: Xerces-backed impl throws non-XMLStreamException so:
        } catch (Exception e) {
            throw new StreamReadException(null, e.getMessage(), e);
        }
        return sr;
    }
}
