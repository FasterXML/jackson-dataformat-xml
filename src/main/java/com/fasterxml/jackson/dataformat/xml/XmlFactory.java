package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import javax.xml.stream.*;

import org.codehaus.stax2.io.Stax2ByteArraySource;
import org.codehaus.stax2.io.Stax2CharArraySource;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.TextualTSFactory;
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
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected int _xmlParserFeatures;

    protected int _xmlGeneratorFeatures;

    // non-final for setters (why are they needed again?)
    protected transient XMLInputFactory _xmlInputFactory;

    protected transient XMLOutputFactory _xmlOutputFactory;

    protected String _cfgNameForTextElement;

    /*
    /**********************************************************
    /* Factory construction, configuration
    /**********************************************************
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
    public XmlFactory() { this(null, null); }

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
        super();
        _xmlParserFeatures = xpFeatures;
        _xmlGeneratorFeatures = xgFeatures;
        _cfgNameForTextElement = nameForTextElem;
        if (xmlIn == null) {
            xmlIn = XMLInputFactory.newInstance();
            // as per [dataformat-xml#190], disable external entity expansion by default
            xmlIn.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            // and ditto wrt [dataformat-xml#211], SUPPORT_DTD
            xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        }
        if (xmlOut == null) {
            xmlOut = XMLOutputFactory.newInstance();
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
    }

    protected XmlFactory(XmlFactory src)
    {
        super(src);
        _xmlParserFeatures = src._xmlParserFeatures;
        _xmlGeneratorFeatures = src._xmlGeneratorFeatures;
        _cfgNameForTextElement = src._cfgNameForTextElement;
        _xmlInputFactory = src._xmlInputFactory;
        _xmlOutputFactory = src._xmlOutputFactory;
    }
    
    protected void _initFactories(XMLInputFactory xmlIn, XMLOutputFactory xmlOut)
    {
        // Better ensure namespaces get built properly, so:
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

    /*
    /**********************************************************
    /* Serializable overrides
    /**********************************************************
     */

    /**
     * Hiding place for JDK-serialization unthawed factories...
     */
    protected transient String _jdkXmlInFactory;

    /**
     * Hiding place for JDK-serialization unthawed factories...
     */
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
        try {
            XMLInputFactory inf = (XMLInputFactory) Class.forName(_jdkXmlInFactory).newInstance();
            XMLOutputFactory outf = (XMLOutputFactory) Class.forName(_jdkXmlOutFactory).newInstance();
            return new XmlFactory(_xmlParserFeatures, _xmlGeneratorFeatures,
                    inf, outf, _cfgNameForTextElement);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
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
    /**********************************************************
    /* Introspection: version, capabilities
    /**********************************************************
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
     * As of 2.4, we do have actual capability for passing char arrays
     * efficiently, but unfortunately
     * have no working mechanism for recycling buffers. So we have to 
     * admit that can not make efficient use.
     */
    @Override
    public boolean canUseCharArrays() { return false; }

    @Override
    public Class<FromXmlParser.Feature> getFormatReadFeatureType() {
        return FromXmlParser.Feature.class;
    }

    @Override
    public Class<ToXmlGenerator.Feature> getFormatWriteFeatureType() {
        return ToXmlGenerator.Feature.class;
    }
    
    /*
    /**********************************************************
    /* Configuration, XML-specific
    /**********************************************************
     */

    public void setXMLTextElementName(String name) {
        _cfgNameForTextElement = name;
    }

    public String getXMLTextElementName() {
        return _cfgNameForTextElement;
    }
    
    /*
    /**********************************************************
    /* Configuration, XML, parser setting
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified XML parser feature.
     */
    public final XmlFactory configure(FromXmlParser.Feature f, boolean state)
    {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Method for enabling specified XML parser feature.
     */
    public XmlFactory enable(FromXmlParser.Feature f) {
        _xmlParserFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified XML parser feature.
     */
    public XmlFactory disable(FromXmlParser.Feature f) {
        _xmlParserFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Checked whether specified XML parser feature is enabled.
     */
    public final boolean isEnabled(FromXmlParser.Feature f) {
        return (_xmlParserFeatures & f.getMask()) != 0;
    }

    /*
    /******************************************************
    /* Configuration, XML, generator settings
    /******************************************************
     */

    /**
     * Method for enabling or disabling specified XML generator feature.
     */
    public final XmlFactory configure(ToXmlGenerator.Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Method for enabling specified XML generator feature.
     */
    public XmlFactory enable(ToXmlGenerator.Feature f) {
        _xmlGeneratorFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified XML generator feature.
     */
    public XmlFactory disable(ToXmlGenerator.Feature f) {
        _xmlGeneratorFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Check whether specified XML generator feature is enabled.
     */
    public final boolean isEnabled(ToXmlGenerator.Feature f) {
        return (_xmlGeneratorFeatures & f.getMask()) != 0;
    }

    /*
    /**********************************************************
    /* Additional configuration
    /**********************************************************
     */

    public XMLInputFactory getXMLInputFactory() {
        return _xmlInputFactory;
    }

    public void setXMLInputFactory(XMLInputFactory f) {
        _xmlInputFactory = f;
    }

    public XMLOutputFactory getXMLOutputFactory() {
        return _xmlOutputFactory;
    }
    
    public void setXMLOutputFactory(XMLOutputFactory f) {
        _xmlOutputFactory = f;
    }

    /*
    /**********************************************************
    /* Format/Schema
    /**********************************************************
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
        return false; // no FormatSchema for json
    }

    /*
    /**********************************************************
    /* Overrides of public methods: parsing
    /**********************************************************
     */

    /**
     * Overridden just to prevent trying to optimize access via char array;
     * while nice idea, problem is that we don't have proper hooks to ensure
     * that temporary buffer gets recycled; so let's just use StringReader.
     */
    @Override
    public JsonParser createParser(ObjectReadContext readCtxt, String content) throws IOException {
        Reader r = new StringReader(content);
        IOContext ioCtxt = _createContext(r, true);
        return _createParser(readCtxt, ioCtxt, _decorate(ioCtxt, r));
    }

    @Override
    protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, DataInput input)
            throws IOException {
        return _unsupported();
    }

    /*
    /**********************************************************
    /* Overrides of public methods: generation
    /**********************************************************
     */

    @Override
    protected JsonGenerator _createGenerator(ObjectWriteContext writeCtxt,
            IOContext ioCtxt, Writer out)
            throws IOException
    {
        // Only care about features and pretty-printer, for now;
        // may add CharacterEscapes in future?
        
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getGeneratorFeatures(_generatorFeatures),
                writeCtxt.getFormatWriteFeatures(_xmlGeneratorFeatures),
                _createXmlWriter(out),
                _xmlPrettyPrinter(writeCtxt));
    }

    @Override
    protected JsonGenerator _createUTF8Generator(ObjectWriteContext writeCtxt,
            IOContext ioCtxt, OutputStream out) throws IOException
    {
        return new ToXmlGenerator(writeCtxt, ioCtxt,
                writeCtxt.getGeneratorFeatures(_generatorFeatures),
                writeCtxt.getFormatWriteFeatures(_xmlGeneratorFeatures),
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
    /**********************************************************
    /* Extended public API, mostly for XmlMapper
    /**********************************************************
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
                readCtxt.getParserFeatures(_parserFeatures),
                readCtxt.getFormatReadFeatures(_xmlParserFeatures),
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
                writeCtxt.getGeneratorFeatures(_generatorFeatures),
                writeCtxt.getFormatWriteFeatures(_xmlGeneratorFeatures),
                sw,
                _xmlPrettyPrinter(writeCtxt));
    }

    /*
    /**********************************************************
    /* Internal factory method overrides
    /**********************************************************
     */

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            InputStream in) throws IOException
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getParserFeatures(_parserFeatures),
                readCtxt.getFormatReadFeatures(_xmlParserFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            Reader r) throws IOException
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(r);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getParserFeatures(_parserFeatures),
                readCtxt.getFormatReadFeatures(_xmlParserFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            char[] data, int offset, int len,
            boolean recycleBuffer) throws IOException
    {
        // !!! TODO: add proper handling of 'recycleBuffer'; currently its handling
        //    is always same as if 'false' was passed
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(new Stax2CharArraySource(data, offset, len));
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getParserFeatures(_parserFeatures),
                readCtxt.getFormatReadFeatures(_xmlParserFeatures),
                sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }
    
    @Override
    protected FromXmlParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            byte[] data, int offset, int len) throws IOException
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(new Stax2ByteArraySource(data, offset, len));
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(readCtxt, ioCtxt,
                readCtxt.getParserFeatures(_parserFeatures),
                readCtxt.getFormatReadFeatures(_xmlParserFeatures),
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

    protected XMLStreamWriter _createXmlWriter(OutputStream out) throws IOException
    {
        XMLStreamWriter sw;
        try {
            sw = _xmlOutputFactory.createXMLStreamWriter(out, "UTF-8");
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsGenerationException(e, null);
        }
        return _initializeXmlWriter(sw);
    }

    protected XMLStreamWriter _createXmlWriter(Writer w) throws IOException
    {
        XMLStreamWriter sw;
        try {
            sw = _xmlOutputFactory.createXMLStreamWriter(w);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsGenerationException(e, null);
        }
        return _initializeXmlWriter(sw);
    }

    protected final XMLStreamWriter _initializeXmlWriter(XMLStreamWriter sw) throws IOException
    {
        // And just for Sun Stax parser (JDK default), seems that we better define default namespace
        // (Woodstox doesn't care) -- otherwise it'll add unnecessary odd declaration
        try {
            sw.setDefaultNamespace("");
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsGenerationException(e, null);
        }
        return sw;
    }

    protected final XMLStreamReader _initializeXmlReader(XMLStreamReader sr) throws IOException
    {
        try {
            // for now, nothing to do... except let's find the root element
            while (sr.next() != XMLStreamConstants.START_ELEMENT) {
                ;
            }
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        return sr;
    }
}
