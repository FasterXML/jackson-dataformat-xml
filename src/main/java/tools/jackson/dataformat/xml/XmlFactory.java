package tools.jackson.dataformat.xml;

import java.io.*;

import javax.xml.stream.*;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.io.Stax2ByteArraySource;
import org.codehaus.stax2.io.Stax2CharArraySource;

import tools.jackson.core.*;
import tools.jackson.core.base.TextualTSFactory;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.core.io.IOContext;

import tools.jackson.dataformat.xml.deser.FromXmlParser;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;
import tools.jackson.dataformat.xml.util.StaxUtil;

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
    final static int DEFAULT_XML_PARSER_FEATURE_FLAGS = XmlReadFeature.collectDefaults();

    /**
     * Bitfield (set of flags) of all generator features that are enabled
     * by default.
     */
    final static int DEFAULT_XML_GENERATOR_FEATURE_FLAGS = XmlWriteFeature.collectDefaults();

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    // !!! 09-Jan-2018, tatu: make final ASAP
    protected String _cfgNameForTextElement;

    protected final XmlNameProcessor _nameProcessor;

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
                xmlIn, xmlOut, XmlNameProcessors.newPassthroughProcessor(),
                null);
    }

    protected XmlFactory(int xpFeatures, int xgFeatures,
            XMLInputFactory xmlIn, XMLOutputFactory xmlOut,
            XmlNameProcessor nameProcessor,
            String nameForTextElem)
    {
        super(StreamReadConstraints.defaults(), StreamWriteConstraints.defaults(),
                ErrorReportConfiguration.defaults(),
                xpFeatures, xgFeatures);
        _nameProcessor = nameProcessor;
        _cfgNameForTextElement = nameForTextElem;
        if (xmlIn == null) {
            xmlIn = XmlFactoryBuilder.defaultXmlInputFactory(getClass().getClassLoader());
        }
        if (xmlOut == null) {
            xmlOut = XmlFactoryBuilder.defaultXmlOutputFactory(getClass().getClassLoader());
        }
        _initFactories(xmlIn, xmlOut);
        _xmlInputFactory = xmlIn;
        _xmlOutputFactory = xmlOut;
    }

    /**
     * Constructors used by {@link XmlFactoryBuilder} for instantiation.
     *
     * @since 3.0
     */
    protected XmlFactory(XmlFactoryBuilder b)
    {
        super(b);
        _cfgNameForTextElement = b.nameForTextElement();
        _xmlInputFactory = b.xmlInputFactory();
        _xmlOutputFactory = b.xmlOutputFactory();
        _nameProcessor = b.xmlNameProcessor();
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
        _nameProcessor = src._nameProcessor;
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
     * The builder returned uses default settings more closely
     * matching the default configs used in Jackson 2.x versions.
     * <p>
     *     This method is still a work in progress and may not yet fully replicate the
     *     default settings of Jackson 2.x.
     * </p>
     */
    public static XmlFactoryBuilder builderWithJackson2Defaults() {
        return builder().configureForJackson2();
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
                inf, outf, _nameProcessor, _cfgNameForTextElement);
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
    public Class<XmlReadFeature> getFormatReadFeatureType() {
        return XmlReadFeature.class;
    }

    @Override
    public Class<XmlWriteFeature> getFormatWriteFeatureType() {
        return XmlWriteFeature.class;
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
        IOContext ioCtxt = _createContext(_createContentReference(content), true);
        return _createParser(readCtxt, ioCtxt, _decorate(ioCtxt, new StringReader(content)));
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
                _xmlPrettyPrinter(writeCtxt),
                _nameProcessor);
    }

    @Override
    protected JsonGenerator _createUTF8Generator(ObjectWriteContext writeCtxt,
            IOContext ioCtxt, OutputStream out)
    {
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getStreamWriteFeatures(_streamWriteFeatures),
                writeCtxt.getFormatWriteFeatures(_formatWriteFeatures),
                _createXmlWriter(out),
                _xmlPrettyPrinter(writeCtxt),
                _nameProcessor);
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
        FromXmlParser xp = new FromXmlParser(readCtxt,
                _createContext(_createContentReference(sr), false),
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr, _nameProcessor);
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
        IOContext ioCtxt = _createContext(_createContentReference(sw), false);
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getStreamWriteFeatures(_streamWriteFeatures),
                writeCtxt.getFormatWriteFeatures(_formatWriteFeatures),
                sw,
                _xmlPrettyPrinter(writeCtxt), _nameProcessor);
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
                sr, _nameProcessor);
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
                sr, _nameProcessor);
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
            // 03-Jul-2021, tatu: [dataformat-xml#482] non-Stax2 impls unlikely to
            //    support so avoid:
            if (_xmlInputFactory instanceof XMLInputFactory2) {
                sr = _xmlInputFactory.createXMLStreamReader(new Stax2CharArraySource(data, offset, len));
            } else {
                sr = _xmlInputFactory.createXMLStreamReader(new CharArrayReader(data, offset, len));
            }
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr, _nameProcessor);
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
            // 03-Jul-2021, tatu: [dataformat-xml#482] non-Stax2 impls unlikely to
            //    support so avoid:
            if (_xmlInputFactory instanceof XMLInputFactory2) {
                sr = _xmlInputFactory.createXMLStreamReader(new Stax2ByteArraySource(data, offset, len));
            } else {
                sr = _xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(data, offset, len));
            }
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getStreamReadFeatures(_streamReadFeatures),
                readCtxt.getFormatReadFeatures(_formatReadFeatures),
                sr, _nameProcessor);
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
