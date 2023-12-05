package com.fasterxml.jackson.dataformat.xml;

import java.io.*;

import javax.xml.stream.*;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.io.Stax2ByteArraySource;
import org.codehaus.stax2.io.Stax2CharArraySource;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
* Factory used for constructing {@link FromXmlParser} and {@link ToXmlGenerator}
* instances.
*<p>
* Implements {@link JsonFactory} since interface for constructing XML backed
* parsers and generators is quite similar to dealing with JSON.
* 
* @author Tatu Saloranta (tatu.saloranta@iki.fi)
*/
public class XmlFactory extends JsonFactory
{
    private static final long serialVersionUID = 1; // 2.6

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

    protected XmlNameProcessor _nameProcessor;
    
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
    public XmlFactory() { this(null, null, null); }

    public XmlFactory(ObjectCodec oc) {
        this(oc, null, null);
    }

    public XmlFactory(XMLInputFactory xmlIn) {
        this(null, xmlIn, null);
    }
    
    public XmlFactory(XMLInputFactory xmlIn, XMLOutputFactory xmlOut) {
        this(null, xmlIn, xmlOut);
    }
    
    public XmlFactory(ObjectCodec oc, XMLInputFactory xmlIn, XMLOutputFactory xmlOut)
    {
        this(oc, DEFAULT_XML_PARSER_FEATURE_FLAGS, DEFAULT_XML_GENERATOR_FEATURE_FLAGS,
                xmlIn, xmlOut, null);
    }

    public XmlFactory(ObjectCodec oc, int xpFeatures, int xgFeatures,
                         XMLInputFactory xmlIn, XMLOutputFactory xmlOut,
                         String nameForTextElem) {
        this(oc, xpFeatures, xgFeatures, xmlIn, xmlOut, nameForTextElem, XmlNameProcessors.newPassthroughProcessor());
    }

    protected XmlFactory(ObjectCodec oc, int xpFeatures, int xgFeatures,
            XMLInputFactory xmlIn, XMLOutputFactory xmlOut,
            String nameForTextElem, XmlNameProcessor nameProcessor)
    {
        super(oc);
        _nameProcessor = nameProcessor;
        _xmlParserFeatures = xpFeatures;
        _xmlGeneratorFeatures = xgFeatures;
        _cfgNameForTextElement = nameForTextElem;
        if (xmlIn == null) {
            xmlIn = StaxUtil.defaultInputFactory(getClass().getClassLoader());
            // as per [dataformat-xml#190], disable external entity expansion by default
            xmlIn.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            // and ditto wrt [dataformat-xml#211], SUPPORT_DTD
            xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        }
        if (xmlOut == null) {
            xmlOut = StaxUtil.defaultOutputFactory(getClass().getClassLoader());
            // [dataformat-xml#326]: Better ensure namespaces get built properly:
            xmlOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        }
        _initFactories(xmlIn, xmlOut);
        _xmlInputFactory = xmlIn;
        _xmlOutputFactory = xmlOut;
    }

    /**
     * @since 2.2.1
     */
    protected XmlFactory(XmlFactory src, ObjectCodec oc)
    {
        super(src, oc);
        _xmlParserFeatures = src._xmlParserFeatures;
        _xmlGeneratorFeatures = src._xmlGeneratorFeatures;
        _cfgNameForTextElement = src._cfgNameForTextElement;
        _xmlInputFactory = src._xmlInputFactory;
        _xmlOutputFactory = src._xmlOutputFactory;
        _nameProcessor = src._nameProcessor;
    }

    /**
     * Constructors used by {@link JsonFactoryBuilder} for instantiation.
     *
     * @since 2.9
     */
    protected XmlFactory(XmlFactoryBuilder b)
    {
        super(b, false);
        _xmlParserFeatures = b.formatParserFeaturesMask();
        _xmlGeneratorFeatures = b.formatGeneratorFeaturesMask();
        _cfgNameForTextElement = b.nameForTextElement();
        _xmlInputFactory = b.xmlInputFactory();
        _xmlOutputFactory = b.xmlOutputFactory();
        _nameProcessor = b.xmlNameProcessor();
        _initFactories(_xmlInputFactory, _xmlOutputFactory);
    }

    public static XmlFactoryBuilder builder() {
        return new XmlFactoryBuilder();
    }

    @Override
    public XmlFactoryBuilder rebuild() {
        return new XmlFactoryBuilder(this);
    }

    protected void _initFactories(XMLInputFactory xmlIn, XMLOutputFactory xmlOut)
    {
        // [dataformat-xml#326]: Better ensure namespaces get built properly, so:
        xmlOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        // and for parser, force coalescing as well (much simpler to use)
        xmlIn.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    }

    /**
     * Note: compared to base implementation by {@link JsonFactory},
     * here the copy will actually share underlying XML input and
     * output factories, as there is no way to make copies of those.
     */
    @Override
    public XmlFactory copy() {
        _checkInvalidCopy(XmlFactory.class);
        return new XmlFactory(this, null);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
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
    @Override // since JsonFactory already implemented it
    protected Object readResolve() {
        if (_jdkXmlInFactory == null) {
            throw new IllegalStateException("No XMLInputFactory class name read during JDK deserialization");
        }
        if (_jdkXmlOutFactory == null) {
            throw new IllegalStateException("No XMLOutputFactory class name read during JDK deserialization");
        }
        final XMLInputFactory inf;
        XMLOutputFactory outf;
        try {
            inf = (XMLInputFactory) Class.forName(_jdkXmlInFactory).getDeclaredConstructor().newInstance();
            outf = (XMLOutputFactory) Class.forName(_jdkXmlOutFactory).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
	return new XmlFactory(_objectCodec, _xmlParserFeatures, _xmlGeneratorFeatures,
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
    /**********************************************************
    /* Configuration, XML-specific
    /**********************************************************
     */
    
    /**
     * @since 2.1
     */
    public void setXMLTextElementName(String name) {
        _cfgNameForTextElement = name;
    }

    /**
     * @since 2.2
     */
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

    @Override
    public int getFormatParserFeatures() {
        return _xmlParserFeatures;
    }

    @Override
    public int getFormatGeneratorFeatures() {
        return _xmlGeneratorFeatures;
    }

    public XmlNameProcessor getXmlNameProcessor() {
        return _nameProcessor;
    }

    public void setXmlNameProcessor(XmlNameProcessor processor) {
        _nameProcessor = processor;
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

    /** @since 2.4 */
    public XMLInputFactory getXMLInputFactory() {
        return _xmlInputFactory;
    }

    /**
     * @deprecated Since 2.12 -- should be set as part of build process
     *   (either in builder, or constructor)
     */
    @Deprecated
    public void setXMLInputFactory(XMLInputFactory f) {
        _xmlInputFactory = f;
    }

    /** @since 2.4 */
    public XMLOutputFactory getXMLOutputFactory() {
        return _xmlOutputFactory;
    }
    
    /**
     * @deprecated Since 2.12 -- should be set as part of build process
     *   (either in builder, or constructor)
     */
    @Deprecated
    public void setXMLOutputFactory(XMLOutputFactory f) {
        _xmlOutputFactory = f;
    }

    /*
    /**********************************************************
    /* Format detection functionality
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
    public MatchStrength hasFormat(InputAccessor acc) throws IOException {
        return hasXMLFormat(acc);
    }

    /**
     * XML format does require support from custom {@link ObjectCodec}
     * (that is, {@link XmlMapper}), so need to return true here.
     * 
     * @return True since XML format does require support from codec
     */
    @Override
    public boolean requiresCustomCodec() { return true; }

    /*
    /**********************************************************
    /* Capability introspection
    /**********************************************************
     */

    /**
     * As of 2.4, we do have actual capability for passing char arrays
     * efficiently, but unfortunately
     * have no working mechanism for recycling buffers. So we have to 
     * admit that can not make efficient use.
     */
    @Override
    public boolean canUseCharArrays() { return false; }

    @Override // since 2.6
    public Class<FromXmlParser.Feature> getFormatReadFeatureType() {
        return FromXmlParser.Feature.class;
    }

    @Override // since 2.6
    public Class<ToXmlGenerator.Feature> getFormatWriteFeatureType() {
        return ToXmlGenerator.Feature.class;
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
    @SuppressWarnings("resource")
    @Override
    public JsonParser createParser(String content) throws IOException {
        Reader r = new StringReader(content);
        IOContext ctxt = _createContext(_createContentReference(r), true);
        if (_inputDecorator != null) {
            r = _inputDecorator.decorate(ctxt, r);
        }
        return _createParser(r, ctxt);
    }
    
    /*
    /**********************************************************
    /* Overrides of public methods: generation
    /**********************************************************
     */

    @Override
    public ToXmlGenerator createGenerator(OutputStream out) throws IOException {
        return createGenerator(out, JsonEncoding.UTF8);
    }
    
    @Override
    public ToXmlGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException
    {
        // false -> we won't manage the stream unless explicitly directed to
        final IOContext ctxt = _createContext(_createContentReference(out), false);
        ctxt.setEncoding(enc);
        return new ToXmlGenerator(ctxt,
                _generatorFeatures, _xmlGeneratorFeatures,
                _objectCodec, _createXmlWriter(ctxt, out), _nameProcessor);
    }
    
    @Override
    public ToXmlGenerator createGenerator(Writer out) throws IOException
    {
        final IOContext ctxt = _createContext(_createContentReference(out), false);
        return new ToXmlGenerator(ctxt,
                _generatorFeatures, _xmlGeneratorFeatures,
                _objectCodec, _createXmlWriter(ctxt, out), _nameProcessor);
    }

    @SuppressWarnings("resource")
    @Override
    public ToXmlGenerator createGenerator(File f, JsonEncoding enc) throws IOException
    {
        OutputStream out = new FileOutputStream(f);
        // true -> yes, we have to manage the stream since we created it
        final IOContext ctxt = _createContext(_createContentReference(out), true);
        ctxt.setEncoding(enc);
        return new ToXmlGenerator(ctxt, _generatorFeatures, _xmlGeneratorFeatures,
                _objectCodec, _createXmlWriter(ctxt, out), _nameProcessor);
    }

    /*
    /**********************************************************
    /* Extended public API, mostly for XmlMapper
    /**********************************************************
     */

    /**
     * Factory method that wraps given {@link XMLStreamReader}, usually to allow
     * partial data-binding.
     * 
     * @since 2.4
     */
    public FromXmlParser createParser(XMLStreamReader sr) throws IOException
    {
        // note: should NOT move parser if already pointing to START_ELEMENT
        if (sr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            sr = _initializeXmlReader(sr);
        }

        // false -> not managed
        FromXmlParser xp = new FromXmlParser(_createContext(_createContentReference(sr), false),
                _parserFeatures, _xmlParserFeatures, _objectCodec, sr, _nameProcessor);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    /**
     * Factory method that wraps given {@link XMLStreamWriter}, usually to allow
     * incremental serialization to compose large output by serializing a sequence
     * of individual objects.
     *
     * @since 2.4
     */
    public ToXmlGenerator createGenerator(XMLStreamWriter sw) throws IOException
    {
        sw = _initializeXmlWriter(sw);
        IOContext ctxt = _createContext(_createContentReference(sw), false);
        return new ToXmlGenerator(ctxt, _generatorFeatures, _xmlGeneratorFeatures,
                _objectCodec, sw, _nameProcessor);
    }

    /*
    /**********************************************************
    /* Internal factory method overrides
    /**********************************************************
     */

    @Override
    protected FromXmlParser _createParser(InputStream in, IOContext ctxt) throws IOException
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, _parserFeatures, _xmlParserFeatures,
                _objectCodec, sr, _nameProcessor);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(Reader r, IOContext ctxt) throws IOException
    {
        XMLStreamReader sr;
        try {
            sr = _xmlInputFactory.createXMLStreamReader(r);
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, _parserFeatures, _xmlParserFeatures,
                _objectCodec, sr, _nameProcessor);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(char[] data, int offset, int len, IOContext ctxt,
            boolean recycleBuffer) throws IOException
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
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, _parserFeatures, _xmlParserFeatures,
                _objectCodec, sr, _nameProcessor);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException
    {
        XMLStreamReader sr;
        try {
            // 03-Jul-2021, tatu: [dataformat-xml#482] non-Stax2 impls unlikely to
            //    support so avoid:
            if (_xmlInputFactory instanceof XMLInputFactory2) {
                sr = _xmlInputFactory.createXMLStreamReader(new Stax2ByteArraySource(data, offset, len));
            } else {
                // 04-Dec-2023, tatu: As per [dataformat-xml#618], JDK's crappy in-built
                //    Stax implementation barfs here. Hence:
                try {
                    sr = _xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(data, offset, len));
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new JsonParseException(null,
                            "Internal processing error by `XMLInputFactory` of type "
                            +ClassUtil.classNameOf(_xmlInputFactory)+" when trying to create a parser ("
                            +"consider using Woodstox instead): "
                            +e.getMessage());
                }
            }
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, null);
        }
        sr = _initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, _parserFeatures, _xmlParserFeatures,
                _objectCodec, sr, _nameProcessor);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        // this method should never get called here, so:
        VersionUtil.throwInternal();
        return null;
    }

    /*
    /**********************************************************************
    /* Internal factory methods, XML-specific
    /**********************************************************************
     */

    protected XMLStreamWriter _createXmlWriter(IOContext ctxt, OutputStream out) throws IOException
    {
        XMLStreamWriter sw;
        try {
            sw = _xmlOutputFactory.createXMLStreamWriter(_decorate(ctxt, out), "UTF-8");
        } catch (Exception e) {
            throw new JsonGenerationException(e.getMessage(), e, null);
        }
        return _initializeXmlWriter(sw);
    }

    protected XMLStreamWriter _createXmlWriter(IOContext ctxt, Writer w) throws IOException
    {
        XMLStreamWriter sw;
        try {
            sw = _xmlOutputFactory.createXMLStreamWriter(_decorate(ctxt, w));
        } catch (Exception e) {
            throw new JsonGenerationException(e.getMessage(), e, null);
        }
        return _initializeXmlWriter(sw);
    }

    protected final XMLStreamWriter _initializeXmlWriter(XMLStreamWriter sw) throws IOException
    {
        // And just for Sun Stax parser (JDK default), seems that we better define default namespace
        // (Woodstox doesn't care) -- otherwise it'll add unnecessary odd declaration
        try {
            sw.setDefaultNamespace("");
        } catch (Exception e) {
            throw new JsonGenerationException(e.getMessage(), e, null);
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
        // [dataformat-xml#350]: Xerces-backed impl throws non-XMLStreamException so:
        } catch (Exception e) {
            throw new JsonParseException(null, e.getMessage(), e);
        }
        return sr;
    }

    /*
    /**********************************************************************
    /* Internal methods, format auto-detection
    /**********************************************************************
     */

    private final static byte UTF8_BOM_1 = (byte) 0xEF;
    private final static byte UTF8_BOM_2 = (byte) 0xBB;
    private final static byte UTF8_BOM_3 = (byte) 0xBF;

    private final static byte BYTE_x = (byte) 'x';
    private final static byte BYTE_m = (byte) 'm';
    private final static byte BYTE_l = (byte) 'l';
    private final static byte BYTE_D = (byte) 'D';

    private final static byte BYTE_LT = (byte) '<';
    private final static byte BYTE_QMARK = (byte) '?';
    private final static byte BYTE_EXCL = (byte) '!';
    private final static byte BYTE_HYPHEN = (byte) '-';
    
    /**
     * Method that tries to figure out if content seems to be in some kind
     * of XML format.
     * Note that implementation here is not nearly as robust as what underlying
     * Stax parser will do; the idea is to first support common encodings,
     * then expand as needed (for example, it is not all that hard to support
     * UTF-16; but it is some work and not needed quite yet)
     */
    public static MatchStrength hasXMLFormat(InputAccessor acc) throws IOException
    {
        /* Basically we just need to find "<!", "<?" or "<NAME"... but ideally
         * we would actually see the XML declaration
         */
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        byte b = acc.nextByte();
        // Very first thing, a UTF-8 BOM? (later improvements: other BOM's, heuristics)
        if (b == UTF8_BOM_1) { // yes, looks like UTF-8 BOM
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != UTF8_BOM_2) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != UTF8_BOM_3) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
        }
        // otherwise: XML declaration?
        boolean maybeXmlDecl = (b == BYTE_LT);
        if (!maybeXmlDecl) {
            int ch = skipSpace(acc, b);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = (byte) ch;
            // If we did not get an LT, shouldn't be valid XML (minus encoding issues etc)
           if (b != BYTE_LT) {
                return MatchStrength.NO_MATCH;
            }
        }
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        b = acc.nextByte();
        // Couple of choices here
        if (b == BYTE_QMARK) { // <?
            b = acc.nextByte();
            if (b == BYTE_x) {
                if (maybeXmlDecl) {
                    if (acc.hasMoreBytes() && acc.nextByte() == BYTE_m) {
                        if (acc.hasMoreBytes() && acc.nextByte() == BYTE_l) {
                            return MatchStrength.FULL_MATCH;
                        }
                    }
                }
                // but even with just partial match, we ought to be fine
                return MatchStrength.SOLID_MATCH;
            }
            // Ok to start with some other char too; just not xml declaration
            if (validXmlNameStartChar(acc, b)) {
                return MatchStrength.SOLID_MATCH;
            }
        } else if (b == BYTE_EXCL) {
            /* must be <!-- comment --> or <!DOCTYPE ...>, since
             * <![CDATA[ ]]> can NOT come outside of root
             */
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
            if (b == BYTE_HYPHEN) {
                if (!acc.hasMoreBytes()) {
                    return MatchStrength.INCONCLUSIVE;
                }
                if (acc.nextByte() == BYTE_HYPHEN) {
                    return MatchStrength.SOLID_MATCH;
                }
            } else if (b == BYTE_D) {
                return tryMatch(acc, "OCTYPE", MatchStrength.SOLID_MATCH);
            }
        } else {
            // maybe root element? Just needs to match first char.
            if (validXmlNameStartChar(acc, b)) {
                return MatchStrength.SOLID_MATCH;
            }
        }
        return MatchStrength.NO_MATCH;
    }

    private final static boolean validXmlNameStartChar(InputAccessor acc, byte b)
        throws IOException
    {
        /* Can make it actual real XML check in future; for now we do just crude
         * check for ASCII range
         */
        int ch = (int) b & 0xFF;
        if (ch >= 'A') { // in theory, colon could be; in practice it should never be valid (wrt namespace)
            // This is where we'd check for multi-byte UTF-8 chars (or whatever encoding is in use)...
            return true;
        }
        return false;
    }
    
    private final static MatchStrength tryMatch(InputAccessor acc, String matchStr, MatchStrength fullMatchStrength)
        throws IOException
    {
        for (int i = 0, len = matchStr.length(); i < len; ++i) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != matchStr.charAt(i)) {
                return MatchStrength.NO_MATCH;
            }
        }
        return fullMatchStrength;
    }
    
    private final static int skipSpace(InputAccessor acc, byte b) throws IOException
    {
        while (true) {
            int ch = (int) b & 0xFF;
            if (!(ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t')) {
                return ch;
            }
            if (!acc.hasMoreBytes()) {
                return -1;
            }
            b = acc.nextByte();
            ch = (int) b & 0xFF;
        }
    }

    /*
    /**********************************************************
    /* Decorators, output
    /**********************************************************
     */

    protected OutputStream _decorate(IOContext ioCtxt, OutputStream out) throws IOException
    {
        if (_outputDecorator != null) {
            OutputStream out2 = _outputDecorator.decorate(ioCtxt, out);
            if (out2 != null) {
                return out2;
            }
        }
        return out;
    }

    protected Writer _decorate(IOContext ioCtxt, Writer out) throws IOException
    {
        if (_outputDecorator != null) {
            Writer out2 = _outputDecorator.decorate(ioCtxt, out);
            if (out2 != null) {
                return out2;
            }
        }
        return out;
    }
}
