package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.JacksonFeatureSet;

import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlNameProcessor;
import com.fasterxml.jackson.dataformat.xml.util.CaseInsensitiveNameSet;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * {@link JsonParser} implementation that exposes XML structure as
 * set of JSON events that can be used for data binding.
 */
public class FromXmlParser
    extends ParserMinimalBase
{
    /**
     * The default name placeholder for XML text segments is empty
     * String ("").
     */
    public final static String DEFAULT_UNNAMED_TEXT_PROPERTY = "";

    /**
     * XML format has some peculiarities, indicated via new (2.12) capability
     * system.
     *
     * @since 2.12
     */
    protected final static JacksonFeatureSet<StreamReadCapability> XML_READ_CAPABILITIES =
            DEFAULT_READ_CAPABILITIES
                .with(StreamReadCapability.DUPLICATE_PROPERTIES)
                .with(StreamReadCapability.SCALARS_AS_OBJECTS)
                .with(StreamReadCapability.UNTYPED_SCALARS)
            ;

    /**
     * Enumeration that defines all togglable features for XML parsers.
     */
    public enum Feature implements FormatFeature
    {
        /**
         * Feature that indicates whether XML Empty elements (ones where there are
         * no separate start and end tags, but just one tag that ends with "/&gt;")
         * are exposed as {@link JsonToken#VALUE_NULL}) or not. If they are not
         * returned as `null` tokens, they will be returned as {@link JsonToken#VALUE_STRING}
         * tokens with textual value of "" (empty String).
         *<p>
         * Default setting was {@code true} (for backwards compatibility from 2.9 to 2.11 (inclusive)
         * but was changed in 2.12 to be {@code false} (see [dataformat-xml#411] for details)
         *
         * @since 2.9
         */
        EMPTY_ELEMENT_AS_NULL(false),

        /**
         * Feature that indicates whether XML Schema Instance attribute
         * {@code xsi:nil} will be processed automatically -- to indicate {@code null}
         * values -- or not.
         * If enabled, {@code xsi:nil} attribute on any XML element will mark such
         * elements as "null values" and any other attributes or child elements they
         * might have to be ignored. If disabled this attribute will be exposed like
         * any other attribute.
         *<p>
         * Default setting is {@code true} since processing was enabled in 2.12.
         *
         * @since 2.13
         */
        PROCESS_XSI_NIL(true),

        // 16-Nov-2020, tatu: would have been nice to add in 2.12 but is not
        //    trivial to implement... so leaving out for now

        /*
         * Feature that indicates whether reading operation should check that
         * the root element's name matches what is expected by read operation:
         * if enabled and name does not match, an exception will be thrown;
         * if disabled, no checking is done (any element name will do).
         *<p>
         * Default setting is {@code true} for backwards compatibility.
         *
         * @since 2.12
        ENFORCE_VALID_ROOT_NAME(false)
         */
        ;

        final boolean _defaultState;
        final int _mask;
        
        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         */
        public static int collectDefaults()
        {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }
        
        private Feature(boolean defaultState) {
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        @Override public boolean enabledByDefault() { return _defaultState; }
        @Override public int getMask() { return _mask; }
        @Override public boolean enabledIn(int flags) { return (flags & getMask()) != 0; }
    }

    /**
     * In cases where a start element has both attributes and non-empty textual
     * value, we have to create a bogus property; we will use this as
     * the property name.
     *<p>
     * Name used for pseudo-property used for returning XML Text value (which does
     * not have actual element name to use). Defaults to empty String, but
     * may be changed for inter-operability reasons: JAXB, for example, uses
     * "value" as name.
     * 
     * @since 2.1
     */
    protected String _cfgNameForTextElement = DEFAULT_UNNAMED_TEXT_PROPERTY;

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link FromXmlParser.Feature}s
     * are enabled.
     */
    protected int _formatFeatures;

    protected ObjectCodec _objectCodec;

    /*
    /**********************************************************
    /* I/O state
    /**********************************************************
     */

    protected final IOContext _ioContext;

    /**
     * @since 2.15
     */
    protected final StreamReadConstraints _streamReadConstraints;

    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Parsing state
    /**********************************************************
     */

    /**
     * Information about parser context, context in which
     * the next token is to be parsed (root, array, object).
     */
    protected XmlReadContext _parsingContext;

    protected final XmlTokenStream _xmlTokens;
    /**
     * 
     * We need special handling to keep track of whether a value
     * may be exposed as simple leaf value.
     */
    protected boolean _mayBeLeaf;

    protected JsonToken _nextToken;

    protected String _currText;

    /**
     * Additional flag that is strictly needed when exposing "mixed" leading
     * String value as "anonymous" property/string pair. If so, code returns
     * START_OBJECT first, sets {@code _nextToken} to be {@code FIELD_NAME}
     * and sets this flag to indicate use of "anonymous" marker.
     *
     * @since 2.13
     */
    protected boolean _nextIsLeadingMixed;

    /*
    /**********************************************************
    /* Parsing state, parsed values
    /**********************************************************
     */

    /**
     * ByteArrayBuilder is needed if 'getBinaryValue' is called. If so,
     * we better reuse it for remainder of content.
     */
    protected ByteArrayBuilder _byteArrayBuilder = null;

    /**
     * We will hold on to decoded binary data, for duration of
     * current event, so that multiple calls to
     * {@link #getBinaryValue} will not need to decode data more
     * than once.
     */
    protected byte[] _binaryValue;

    /*
    /**********************************************************
    /* Parsing state, number decoding (2.12+)
    /**********************************************************
     */

    /**
     * Bitfield that indicates which numeric representations
     * have been calculated for the current type
     */
    protected int _numTypesValid = NR_UNKNOWN;

    // First primitives

    protected int _numberInt;
    protected long _numberLong;

    // And then object types

    protected BigInteger _numberBigInt;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public FromXmlParser(IOContext ctxt, int genericParserFeatures, int xmlFeatures,
             ObjectCodec codec, XMLStreamReader xmlReader, XmlNameProcessor tagProcessor)
        throws IOException
    {
        super(genericParserFeatures);
        _formatFeatures = xmlFeatures;
        _ioContext = ctxt;
        _streamReadConstraints = ctxt.streamReadConstraints();
        _objectCodec = codec;
        _parsingContext = XmlReadContext.createRootContext(-1, -1);
        _xmlTokens = new XmlTokenStream(xmlReader, ctxt.contentReference(),
                    _formatFeatures, tagProcessor);

        final int firstToken;
        try {
            firstToken = _xmlTokens.initialize();
        } catch (XMLStreamException e) {
            StaxUtil.throwAsParseException(e, this);
            return;
        }

        // 04-Jan-2019, tatu: Root-level nulls need slightly specific handling;
        //    changed in 2.10.2
        if (_xmlTokens.hasXsiNil()) {
            _nextToken = JsonToken.VALUE_NULL;
        } else {
            switch (firstToken) {
            case XmlTokenStream.XML_START_ELEMENT:
            // Removed from 2.14:
            // case XmlTokenStream.XML_DELAYED_START_ELEMENT:
                _nextToken = JsonToken.START_OBJECT;
                break;
            case XmlTokenStream.XML_ROOT_TEXT:
                _currText = _xmlTokens.getText();
                // [dataformat-xml#435]: may get `null` from empty element...
                // It's complicated.
                if (_currText == null) {
                    _nextToken = JsonToken.VALUE_NULL;
                } else {
                    _nextToken = JsonToken.VALUE_STRING;
                }
                break;
            default:
                _reportError("Internal problem: invalid starting state (%s)", _xmlTokens._currentStateDesc());
            }
        }
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }
    
    @Override
    public ObjectCodec getCodec() {
        return _objectCodec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        _objectCodec = c;
    }

    /**
     * @since 2.1
     */
    public void setXMLTextElementName(String name) {
        _cfgNameForTextElement = name;
    }

    /*
    /**********************************************************************
    /* Overrides: capability introspection methods
    /**********************************************************************
     */

    /**
     * XML format does require support from custom {@link ObjectCodec}
     * (that is, {@link XmlMapper}), so need to return true here.
     * 
     * @return True since XML format does require support from codec
     */
    @Override
    public boolean requiresCustomCodec() {
        return true;
    }

    @Override
    public boolean canReadObjectId() { return false; }

    @Override
    public boolean canReadTypeId() { return false; }

    @Override
    public JacksonFeatureSet<StreamReadCapability> getReadCapabilities() {
        return XML_READ_CAPABILITIES;
    }

    /*
    /**********************************************************
    /* Extended API, configuration
    /**********************************************************
     */

    public FromXmlParser enable(Feature f) {
        _formatFeatures |= f.getMask();
        _xmlTokens.setFormatFeatures(_formatFeatures);
        return this;
    }

    public FromXmlParser disable(Feature f) {
        _formatFeatures &= ~f.getMask();
        _xmlTokens.setFormatFeatures(_formatFeatures);
        return this;
    }

    public final boolean isEnabled(Feature f) {
        return (_formatFeatures & f.getMask()) != 0;
    }

    public FromXmlParser configure(Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    @Override
    public StreamReadConstraints streamReadConstraints() {
        return _streamReadConstraints;
    }

    /*
    /**********************************************************                              
    /* FormatFeature support                                                                             
    /**********************************************************                              
     */

    @Override
    public int getFormatFeatures() {
        return _formatFeatures;
    }

    @Override
    public JsonParser overrideFormatFeatures(int values, int mask) {
        _formatFeatures = (_formatFeatures & ~mask) | (values & mask);
        _xmlTokens.setFormatFeatures(_formatFeatures);
        return this;
    }

    /*
    /**********************************************************
    /* Extended API, access to some internal components
    /**********************************************************
     */

    /**
     * Method that allows application direct access to underlying
     * Stax {@link XMLStreamWriter}. Note that use of writer is
     * discouraged, and may interfere with processing of this writer;
     * however, occasionally it may be necessary.
     *<p>
     * Note: writer instance will always be of type
     * {@link org.codehaus.stax2.XMLStreamWriter2} (including
     * Typed Access API) so upcasts are safe.
     */
    public XMLStreamReader getStaxReader() {
        return _xmlTokens.getXmlReader();
    }

    /*
    /**********************************************************
    /* Internal API
    /**********************************************************
     */

    /**
     * Method that may be called to indicate that specified names
     * (only local parts retained currently: this may be changed in
     * future) should be considered "auto-wrapping", meaning that
     * they will be doubled to contain two opening elements, two
     * matching closing elements. This is needed for supporting
     * handling of so-called "unwrapped" array types, something
     * XML mappings like JAXB often use.
     *<p>
     * NOTE: this method is considered part of internal implementation
     * interface, and it is <b>NOT</b> guaranteed to remain unchanged
     * between minor versions (it is however expected not to change in
     * patch versions). So if you have to use it, be prepared for
     * possible additional work.
     * 
     * @since 2.12
     */
    public void addVirtualWrapping(Set<String> namesToWrap0, boolean caseInsensitive)
    {
//System.out.printf("addVirtualWrapping(%s) at '%s' [case-insensitive? %s]\n", namesToWrap0, _parsingContext.pathAsPointer(), caseInsensitive);

        final Set<String> namesToWrap = caseInsensitive
                ? CaseInsensitiveNameSet.construct(namesToWrap0)
                : namesToWrap0;

        // 17-Sep-2012, tatu: Not 100% sure why, but this is necessary to avoid
        //   problems with Lists-in-Lists properties
        // 12-May-2020, tatu: But as per [dataformat-xml#86] NOT for root element
        //   (would still like to know why work-around needed ever, but...)
        if (!_parsingContext.inRoot()
                 && !_parsingContext.getParent().inRoot()) {
            String name = _xmlTokens.getLocalName();
            if ((name != null) && namesToWrap.contains(name)) {
//System.out.println("REPEAT from addVirtualWrapping() for '"+name+"'");
                _xmlTokens.repeatStartElement();
            }
        }
        _parsingContext.setNamesToWrap(namesToWrap);
    }

    @Deprecated // since 2.12
    public void addVirtualWrapping(Set<String> namesToWrap) {
        addVirtualWrapping(namesToWrap, false);
    }

    /*
    /**********************************************************
    /* JsonParser impl
    /**********************************************************
     */
    
    /**
     * Method that can be called to get the name associated with
     * the current event.
     */
    @Override
    public String getCurrentName() throws IOException
    {
        // start markers require information from parent
        String name;
        if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
            XmlReadContext parent = _parsingContext.getParent();
            name = parent.getCurrentName();
        } else {
            name = _parsingContext.getCurrentName();
        }
        // sanity check
        if (name == null) {
            throw new IllegalStateException("Missing name, in state: "+_currToken);
        }
        return name;
    }

    @Override
    public void overrideCurrentName(String name)
    {
        // Simple, but need to look for START_OBJECT/ARRAY's "off-by-one" thing:
        XmlReadContext ctxt = _parsingContext;
        if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
            ctxt = ctxt.getParent();
        }
        ctxt.setCurrentName(name);
    }
    
    @Override
    public void close() throws IOException
    {
        if (!_closed) {
            _closed = true;
            try {
                if (_ioContext.isResourceManaged() || isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE)) {
                    _xmlTokens.closeCompletely();
                } else {
                    _xmlTokens.close();
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwAsParseException(e, this);
            } finally {
                // Also, internal buffer(s) can now be released as well
                _releaseBuffers();
            }
        }
    }

    @Override
    public boolean isClosed() { return _closed; }

    @Override
    public XmlReadContext getParsingContext() {
        return _parsingContext;
    }

    /**
     * Method that return the <b>starting</b> location of the current
     * token; that is, position of the first character from input
     * that starts the current token.
     */
    @Override
    public JsonLocation getTokenLocation() {
        return _xmlTokens.getTokenLocation();
    }

    /**
     * Method that returns location of the last processed character;
     * usually for error reporting purposes
     */
    @Override
    public JsonLocation getCurrentLocation() {
        return _xmlTokens.getCurrentLocation();
    }

    /**
     * Since xml representation can not really distinguish between array
     * and object starts (both are represented with elements), this method
     * is overridden and taken to mean that expecation is that the current
     * start element is to mean 'start array', instead of default of
     * 'start object'.
     */
    @Override
    public boolean isExpectedStartArrayToken()
    {
        JsonToken t = _currToken;
        if (t == JsonToken.START_OBJECT) {
            _currToken = JsonToken.START_ARRAY;
            // Ok: must replace current context with array as well
            _parsingContext.convertToArray();
//System.out.println(" FromXmlParser.isExpectedArrayStart(): OBJ->Array");
            // And just in case a field name was to be returned, wipe it
            // 06-Jan-2015, tatu: Actually, could also be empty Object buffered; if so, convert...
            if (_nextToken == JsonToken.END_OBJECT) {
                _nextToken = JsonToken.END_ARRAY;
            } else {
                _nextToken = null;
            }
            // and last thing, [dataformat-xml#33], better ignore attributes
            _xmlTokens.skipAttributes();
            return true;
        }
//System.out.println(" FromXmlParser.isExpectedArrayStart?: t="+t);
        return (t == JsonToken.START_ARRAY);
    }

    /**
     * Since xml representation can not really distinguish between different
     * scalar types (numbers, booleans) -- they are all just Character Data,
     * without schema -- we can try to infer type from intent here.
     * The main benefit is avoiding checks for coercion.
     */
    @Override
    public boolean isExpectedNumberIntToken()
    {
        JsonToken t = _currToken;
        if (t == JsonToken.VALUE_STRING) {
            final String text = _currText.trim();
            final int len = _isIntNumber(text);
            if (len > 0) {
                if (len <= 9) {
                    _numberInt = NumberInput.parseInt(text);
                    _numTypesValid = NR_INT;
                    _currToken = JsonToken.VALUE_NUMBER_INT;
                    return true;
                }
                if (len <= 18) { // definitely in long range
                    long l = NumberInput.parseLong(text);
                    if (len == 10) {
                        int asInt = (int) l;
                        long l2 = (long) asInt;
                        if (l == l2) {
                            _numberInt = asInt;
                            _numTypesValid = NR_INT;
                            _currToken = JsonToken.VALUE_NUMBER_INT;
                            return true;
                        }
                    }
                    _numberLong = l;
                    _numTypesValid = NR_LONG;
                    _currToken = JsonToken.VALUE_NUMBER_INT;
                    return true;
                }
                // Might still fit within `long`
                if (len == 19) {
                    final boolean stillLong;
                    if (text.charAt(0) == '-') {
                        stillLong = NumberInput.inLongRange(text.substring(1), true);
                    } else {
                        stillLong = NumberInput.inLongRange(text, false);
                    }
                    if (stillLong) {
                        _numberLong = NumberInput.parseLong(text);
                        _numTypesValid = NR_LONG;
                        _currToken = JsonToken.VALUE_NUMBER_INT;
                        return true;
                    }
                }
                // finally, need BigInteger
                streamReadConstraints().validateIntegerLength(text.length());
                _numberBigInt = NumberInput.parseBigInteger(
                        text, isEnabled(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER));
                _numTypesValid = NR_BIGINT;
                _currToken = JsonToken.VALUE_NUMBER_INT;
                return true;
            }
        }
        return (t == JsonToken.VALUE_NUMBER_INT);
    }

    // DEBUGGING
    /*
    @Override
    public JsonToken nextToken() throws IOException
    {
        JsonToken t = nextToken0();
        if (t != null) {
            final String loc = (_parsingContext == null) ? "NULL" : String.valueOf(_parsingContext.pathAsPointer());
            switch (t) {
            case FIELD_NAME:
                System.out.printf("FromXmlParser.nextToken() at '%s': JsonToken.FIELD_NAME '%s'\n", loc, _parsingContext.getCurrentName());
                break;
            case VALUE_STRING:
                System.out.printf("FromXmlParser.nextToken() at '%s': JsonToken.VALUE_STRING '%s'\n", loc, getText());
                break;
            default:
                System.out.printf("FromXmlParser.nextToken() at '%s': %s\n", loc, t);
            }
        }
        return t;
    }
    */

//    public JsonToken nextToken0() throws IOException
    @Override
    public JsonToken nextToken() throws IOException
    {
        _binaryValue = null;
        _numTypesValid = NR_UNKNOWN;
//System.out.println("FromXmlParser.nextToken0: _nextToken = "+_nextToken);
        if (_nextToken != null) {
            JsonToken t = _nextToken;
            _currToken = t;
            _nextToken = null;

            switch (t) {
            case START_OBJECT:
                _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                break;
            case START_ARRAY:
                _parsingContext = _parsingContext.createChildArrayContext(-1, -1);
                break;
            case END_OBJECT:
            case END_ARRAY:
                _parsingContext = _parsingContext.getParent();
                break;
            case FIELD_NAME:
                // 29-Mar-2021, tatu: [dataformat-xml#442]: special case of leading
                //    mixed text added
                if (_nextIsLeadingMixed) {
                    _nextIsLeadingMixed = false;
                    _parsingContext.setCurrentName(_cfgNameForTextElement);
                    _nextToken = JsonToken.VALUE_STRING;
                } else {
                    _parsingContext.setCurrentName(_xmlTokens.getLocalName());
                }
                break;
            default: // VALUE_STRING, VALUE_NULL
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index` anyway; not
                //    used for Object contexts, updated automatically by "createChildXxxContext"
                _parsingContext.valueStarted();
            }
            return t;
        }

        int token = _nextToken();
        // Need to have a loop just because we may have to eat/convert
        // a start-element that indicates an array element.
        while (token == XmlTokenStream.XML_START_ELEMENT) {
            // If we thought we might get leaf, no such luck
            if (_mayBeLeaf) {
                // leave _mayBeLeaf set, as we start a new context
                _nextToken = JsonToken.FIELD_NAME;
                _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                return (_currToken = JsonToken.START_OBJECT);
            }
            if (_parsingContext.inArray()) {
                // Yup: in array, so this element could be verified; but it won't be
                // reported anyway, and we need to process following event.
                token = _nextToken();
                _mayBeLeaf = true;
                continue;
            }
            String name = _xmlTokens.getLocalName();
            _parsingContext.setCurrentName(name);

            // Ok: virtual wrapping can be done by simply repeating current START_ELEMENT.
            // Couple of ways to do it; but start by making _xmlTokens replay the thing...
            if (_parsingContext.shouldWrap(name)) {
                _xmlTokens.repeatStartElement();
            }

            _mayBeLeaf = true;
            // Ok: in array context we need to skip reporting field names.
            // But what's the best way to find next token?
            return (_currToken = JsonToken.FIELD_NAME);
        }

        // Ok; beyond start element, what do we get?
        while (true) {
            switch (token) {
            case XmlTokenStream.XML_END_ELEMENT:
                // Simple, except that if this is a leaf, need to suppress end:
                if (_mayBeLeaf) {
                    _mayBeLeaf = false;
                    if (_parsingContext.inArray()) {
                        // 06-Jan-2015, tatu: as per [dataformat-xml#180], need to
                        //    expose as empty Object, not null
                        _nextToken = JsonToken.END_OBJECT;
                        _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                        return (_currToken = JsonToken.START_OBJECT);
                    }
                    // 07-Sep-2019, tatu: for [dataformat-xml#353], must NOT return second null
                    if (_currToken != JsonToken.VALUE_NULL) {
                        // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                        _parsingContext.valueStarted();
                        return (_currToken = JsonToken.VALUE_NULL);
                    }
                }
                _currToken = _parsingContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
                _parsingContext = _parsingContext.getParent();
                return _currToken;

            case XmlTokenStream.XML_ATTRIBUTE_NAME:
                // If there was a chance of leaf node, no more...
                if (_mayBeLeaf) {
                    _mayBeLeaf = false;
                    _nextToken = JsonToken.FIELD_NAME;
                    _currText = _xmlTokens.getText();
                    _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                    return (_currToken = JsonToken.START_OBJECT);
                }
                _parsingContext.setCurrentName(_xmlTokens.getLocalName());
                return (_currToken = JsonToken.FIELD_NAME);
            case XmlTokenStream.XML_ATTRIBUTE_VALUE:
                _currText = _xmlTokens.getText();
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _parsingContext.valueStarted();
                return (_currToken = JsonToken.VALUE_STRING);
            case XmlTokenStream.XML_TEXT:
                _currText = _xmlTokens.getText();
                if (_mayBeLeaf) {
                    _mayBeLeaf = false;
                    // One more refinement (pronounced like "hack") is that if
                    // we had an empty String (or all white space), and we are
                    // deserializing an array, we better hide the empty text.
                    // Also: must skip following END_ELEMENT
                    // 05-Jun-2020, tatu: ... if there is one; we may actually alternatively
                    //   get START_ELEMENT for "mixed content" case; if so, need to change to
                    //   expose "XmlText" as separate property
                    token = _nextToken();

                    if (token == XmlTokenStream.XML_END_ELEMENT) {
                        if (_parsingContext.inArray()) {
                            if (XmlTokenStream._allWs(_currText)) {
                                // 06-Jan-2015, tatu: as per [dataformat-xml#180], need to
                                //    expose as empty Object, not null (or, worse, as used to
                                //    be done, by swallowing the token)
                                _nextToken = JsonToken.END_OBJECT;
                                _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                                return (_currToken = JsonToken.START_OBJECT);
                            }
                        }
                        return (_currToken = JsonToken.VALUE_STRING);
                    }
                    if (token != XmlTokenStream.XML_START_ELEMENT) {
                        throw new JsonParseException(this, String.format(
"Internal error: Expected END_ELEMENT (%d) or START_ELEMENT (%d), got event of type %d",
XmlTokenStream.XML_END_ELEMENT, XmlTokenStream.XML_START_ELEMENT, token));
                    }
                    // fall-through, except must create new context AND push back
                    // START_ELEMENT we just saw:
                    _xmlTokens.pushbackCurrentToken();
                    _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                }
                // [dataformat-xml#177]: empty text may also need to be skipped
                // but... [dataformat-xml#191]: looks like we can't short-cut, must
                // loop over again
                if (_parsingContext.inObject()) {
                    if (_currToken == JsonToken.FIELD_NAME) {
                        // 29-Mar-2021, tatu: [dataformat-xml#442]: need special handling for
                        //    leading mixed content; requires 3-token sequence for which _nextToken
                        //    along is not enough.
                        _nextIsLeadingMixed = true;
                        _nextToken = JsonToken.FIELD_NAME;
                        return (_currToken = JsonToken.START_OBJECT);
                    } else if (XmlTokenStream._allWs(_currText)) {
                        token = _nextToken();
                        continue;
                    }
                } else if (_parsingContext.inArray()) {
                    // [dataformat-xml#319] Aaaaand for Arrays too
                    if (XmlTokenStream._allWs(_currText)) {
                        token = _nextToken();
                        continue;
                    }
                    // 29-Mar-2021, tatu: This seems like an error condition...
                    //   How should we indicate it? As of 2.13, report as unexpected state
                    throw _constructError(
"Unexpected non-whitespace text ('"+_currText+"' in Array context: should not occur (or should be handled)"
);
                }

                // If not a leaf (or otherwise ignorable), need to transform into property...
                _parsingContext.setCurrentName(_cfgNameForTextElement);
                _nextToken = JsonToken.VALUE_STRING;
                return (_currToken = JsonToken.FIELD_NAME);
            case XmlTokenStream.XML_END:
                return (_currToken = null);
            default:
                return _internalErrorUnknownToken(token);
            }
        }
    }

    /*
    /**********************************************************
    /* Overrides of specialized nextXxx() methods
    /**********************************************************
     */

    /*
    @Override
    public String nextFieldName() throws IOException {
        if (nextToken() == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return null;
    }
    */

    /**
     * Method overridden to support more reliable deserialization of
     * String collections.
     */
    @Override
    public String nextTextValue() throws IOException
    {
        _binaryValue = null;
        if (_nextToken != null) {
            JsonToken t = _nextToken;
            _currToken = t;
            _nextToken = null;

            // expected case; yes, got a String
            if (t == JsonToken.VALUE_STRING) {
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _parsingContext.valueStarted();
                return _currText;
            }
            _updateState(t);
            return null;
        }

        int token = _nextToken();

        // mostly copied from 'nextToken()'
        while (token == XmlTokenStream.XML_START_ELEMENT) {
            if (_mayBeLeaf) {
                _nextToken = JsonToken.FIELD_NAME;
                _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                _currToken = JsonToken.START_OBJECT;
                return null;
            }
            if (_parsingContext.inArray()) {
                token = _nextToken();
                _mayBeLeaf = true;
                continue;
            }
            String name = _xmlTokens.getLocalName();
            _parsingContext.setCurrentName(name);
            if (_parsingContext.shouldWrap(name)) {
//System.out.println("REPEAT from nextTextValue()");
                _xmlTokens.repeatStartElement();
            }
            _mayBeLeaf = true;
            _currToken = JsonToken.FIELD_NAME;
            return null;
        }

        // Ok; beyond start element, what do we get?
        switch (token) {
        case XmlTokenStream.XML_END_ELEMENT:
            if (_mayBeLeaf) {
                // NOTE: this is different from nextToken() -- produce "", NOT null
                _mayBeLeaf = false;
                _currToken = JsonToken.VALUE_STRING;
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _parsingContext.valueStarted();
                return (_currText = "");
            }
            _currToken = _parsingContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
            _parsingContext = _parsingContext.getParent();
            break;
        case XmlTokenStream.XML_ATTRIBUTE_NAME:
            // If there was a chance of leaf node, no more...
            if (_mayBeLeaf) {
                _mayBeLeaf = false;
                _nextToken = JsonToken.FIELD_NAME;
                _currText = _xmlTokens.getText();
                _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                _currToken = JsonToken.START_OBJECT;
            } else {
                _parsingContext.setCurrentName(_xmlTokens.getLocalName());
                _currToken = JsonToken.FIELD_NAME;
            }
            break;
        case XmlTokenStream.XML_ATTRIBUTE_VALUE:
            _currToken = JsonToken.VALUE_STRING;
            // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
            _parsingContext.valueStarted();
            return (_currText = _xmlTokens.getText());
        case XmlTokenStream.XML_TEXT:
            _currText = _xmlTokens.getText();
            if (_mayBeLeaf) {
                _mayBeLeaf = false;
                // Also: must skip following END_ELEMENT
                _skipEndElement();
                // NOTE: this is different from nextToken() -- NO work-around
                // for otherwise empty List/array
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _parsingContext.valueStarted();
                _currToken = JsonToken.VALUE_STRING;
                return _currText;
            }
            // If not a leaf, need to transform into property...
            _parsingContext.setCurrentName(_cfgNameForTextElement);
            _nextToken = JsonToken.VALUE_STRING;
            _currToken = JsonToken.FIELD_NAME;
            break;
        case XmlTokenStream.XML_END:
            _currToken = null;
        default:
            return _internalErrorUnknownToken(token);
        }
        return null;
    }


    private void _updateState(JsonToken t)
    {
        switch (t) {
        case START_OBJECT:
            _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
            break;
        case START_ARRAY:
            _parsingContext = _parsingContext.createChildArrayContext(-1, -1);
            break;
        case END_OBJECT:
        case END_ARRAY:
            _parsingContext = _parsingContext.getParent();
            break;
        case FIELD_NAME:
            _parsingContext.setCurrentName(_xmlTokens.getLocalName());
            break;
        default:
            _internalErrorUnknownToken(t);
        }
    }

    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    @Override
    public String getText() throws IOException
    {
        if (_currToken == null) {
            return null;
        }
        switch (_currToken) {
        case FIELD_NAME:
            return getCurrentName();
        case VALUE_STRING:
            return _currText;
        default:
            return _currToken.asString();
        }
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        String text = getText();
        return (text == null)  ? null : text.toCharArray();
    }

    @Override
    public int getTextLength() throws IOException {
        String text = getText();
        return (text == null)  ? 0 : text.length();
    }

    @Override
    public int getTextOffset() throws IOException {
        return 0;
    }

    /**
     * XML input actually would offer access to character arrays; but since
     * we must coalesce things it cannot really be exposed.
     */
    @Override
    public boolean hasTextCharacters()
    {
        return false;
    }

    @Override // since 2.8
    public int getText(Writer writer) throws IOException
    {
        String str = getText();
        if (str == null) {
            return 0;
        }
        writer.write(str);
        return str.length();
    }

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    @Override
    public Object getEmbeddedObject() throws IOException {
        // no way to embed POJOs for now...
        return null;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException
    {
        if (_currToken != JsonToken.VALUE_STRING &&
                (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT || _binaryValue == null)) {
            _reportError("Current token ("+_currToken+") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        /* To ensure that we won't see inconsistent data, better clear up
         * state...
         */
        if (_binaryValue == null) {
            try {
                _binaryValue = _decodeBase64(b64variant);
            } catch (IllegalArgumentException iae) {
                throw _constructError("Failed to decode VALUE_STRING as base64 ("+b64variant+"): "+iae.getMessage());
            }
        }        
        return _binaryValue;
    }

    @SuppressWarnings("resource")
    protected byte[] _decodeBase64(Base64Variant b64variant) throws IOException
    {
        ByteArrayBuilder builder = _getByteArrayBuilder();
        final String str = getText();
        _decodeBase64(str, builder, b64variant);
        return builder.toByteArray();
    }

    /*
    /**********************************************************
    /* Numeric accessors (implemented since 2.12)
    /**********************************************************
     */

    @Override
    public boolean isNaN() {
        return false; // can't have since we only coerce integers
    }

    @Override
    public NumberType getNumberType() throws IOException {
        if (_numTypesValid == NR_UNKNOWN) {
            _checkNumericValue(NR_UNKNOWN); // will also check event type
        }
        // Only integer types supported so...
        
        if ((_numTypesValid & NR_INT) != 0) {
            return NumberType.INT;
        }
        if ((_numTypesValid & NR_LONG) != 0) {
            return NumberType.LONG;
        }
        return NumberType.BIG_INTEGER;
    }

    @Override
    public Number getNumberValue() throws IOException {
        if (_numTypesValid == NR_UNKNOWN) {
            _checkNumericValue(NR_UNKNOWN); // will also check event type
        }
        // Only integer types supported so...

        if ((_numTypesValid & NR_INT) != 0) {
            return _numberInt;
        }
        if ((_numTypesValid & NR_LONG) != 0) {
            return _numberLong;
        }
        if ((_numTypesValid & NR_BIGINT) != 0) {
            return _numberBigInt;
        }
        _throwInternal();
        return null;
    }

    @Override
    public int getIntValue() throws IOException {
        if ((_numTypesValid & NR_INT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) { // not parsed at all
                _checkNumericValue(NR_INT); // will also check event type
            }
            if ((_numTypesValid & NR_INT) == 0) { // wasn't an int natively?
                _convertNumberToInt(); // let's make it so, if possible
            }
        }
        return _numberInt;
    }

    @Override
    public long getLongValue() throws IOException {
        if ((_numTypesValid & NR_LONG) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_LONG);
            }
            if ((_numTypesValid & NR_LONG) == 0) {
                _convertNumberToLong();
            }
        }
        return _numberLong;
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        if ((_numTypesValid & NR_BIGINT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_BIGINT);
            }
            if ((_numTypesValid & NR_BIGINT) == 0) {
                _convertNumberToBigInteger();
            }
        }
        return _numberBigInt;
    }

    @Override
    public float getFloatValue() throws IOException {
        if ((_numTypesValid & NR_FLOAT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_FLOAT);
            }
        }
        return _convertNumberToFloat();
    }

    @Override
    public double getDoubleValue() throws IOException {
        if ((_numTypesValid & NR_DOUBLE) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_DOUBLE);
            }
        }
        return _convertNumberToDouble();
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        if ((_numTypesValid & NR_BIGDECIMAL) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_BIGDECIMAL);
            }
        }
        return _convertNumberToBigDecimal();
    }

    // // // Helper methods for Numeric accessors

    protected final void _checkNumericValue(int expType) throws IOException {
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            return;
        }
        _reportError("Current token ("+currentToken()+") not numeric, can not use numeric value accessors");
    }

    // NOTE: copied from `StdDeserializer`...
    protected final int _isIntNumber(String text)
    {
        final int len = text.length();
        if (len > 0) {
            char c = text.charAt(0);
            // skip leading negative sign, do NOT allow leading plus
            final int start = (c == '-') ? 1 : 0;
            for (int i = start; i < len; ++i) {
                int ch = text.charAt(i);
                if (ch > '9' || ch < '0') {
                    return -1;
                }
            }
            return len - start;
        }
        return 0;
    }

    protected void _convertNumberToInt() throws IOException
    {
        // First, converting from long ought to be easy
        if ((_numTypesValid & NR_LONG) != 0) {
            // Let's verify it's lossless conversion by simple roundtrip
            int result = (int) _numberLong;
            if (((long) result) != _numberLong) {
                _reportError("Numeric value ("+getText()+") out of range of int");
            }
            _numberInt = result;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (BI_MIN_INT.compareTo(_numberBigInt) > 0 
                    || BI_MAX_INT.compareTo(_numberBigInt) < 0) {
                reportOverflowInt();
            }
            _numberInt = _numberBigInt.intValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_INT;
    }
    
    protected void _convertNumberToLong() throws IOException
    {
        if ((_numTypesValid & NR_INT) != 0) {
            _numberLong = (long) _numberInt;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (BI_MIN_LONG.compareTo(_numberBigInt) > 0 
                    || BI_MAX_LONG.compareTo(_numberBigInt) < 0) {
                reportOverflowLong();
            }
            _numberLong = _numberBigInt.longValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_LONG;
    }
    
    protected void _convertNumberToBigInteger() throws IOException
    {
        if ((_numTypesValid & NR_LONG) != 0) {
            _numberBigInt = BigInteger.valueOf(_numberLong);
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberBigInt = BigInteger.valueOf(_numberInt);
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_BIGINT;
    }

    protected float _convertNumberToFloat() throws IOException
    {
        // Note: this MUST start with more accurate representations, since we don't know which
        //  value is the original one (others get generated when requested)
        if ((_numTypesValid & NR_BIGINT) != 0) {
            return _numberBigInt.floatValue();
        }
        if ((_numTypesValid & NR_LONG) != 0) {
            return (float) _numberLong;
        }
        if ((_numTypesValid & NR_INT) != 0) {
            return (float) _numberInt;
        }
        _throwInternal();
        return 0.0f;
    }
    
    protected double _convertNumberToDouble() throws IOException
    {
        // same as above, start from more to less accurate
        if ((_numTypesValid & NR_BIGINT) != 0) {
            return _numberBigInt.doubleValue();
        }
        if ((_numTypesValid & NR_LONG) != 0) {
            return (double) _numberLong;
        }
        if ((_numTypesValid & NR_INT) != 0) {
            return (double) _numberInt;
        }
        _throwInternal();
        return 0.0;
    }

    protected BigDecimal _convertNumberToBigDecimal() throws IOException
    {
        if ((_numTypesValid & NR_BIGINT) != 0) {
            return new BigDecimal(_numberBigInt);
        }
        if ((_numTypesValid & NR_LONG) != 0) {
            return BigDecimal.valueOf(_numberLong);
        }
        if ((_numTypesValid & NR_INT) != 0) {
            return BigDecimal.valueOf(_numberInt);
        }
        _throwInternal();
        return null;
    }

    /*
    /**********************************************************
    /* Abstract method impls for stuff from JsonParser
    /**********************************************************
     */

    /**
     * Method called when an EOF is encountered between tokens.
     * If so, it may be a legitimate EOF, but only iff there
     * is no open non-root context.
     */
    @Override
    protected void _handleEOF() throws JsonParseException
    {
        if (!_parsingContext.inRoot()) {
            String marker = _parsingContext.inArray() ? "Array" : "Object";
            _reportInvalidEOF(String.format(
                    ": expected close marker for %s (start marker at %s)",
                    marker,
                    _parsingContext.startLocation(_ioContext.contentReference())),
                    null);
        }
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Method called to release internal buffers owned by the base
     * parser.
     */
    protected void _releaseBuffers() throws IOException {
        // anything we can/must release? Underlying parser should do all of it, for now?
    }

    protected ByteArrayBuilder _getByteArrayBuilder()
    {
        if (_byteArrayBuilder == null) {
            _byteArrayBuilder = new ByteArrayBuilder();
        } else {
            _byteArrayBuilder.reset();
        }
        return _byteArrayBuilder;
    }

    private <T> T _internalErrorUnknownToken(Object token) {
        throw new IllegalStateException("Internal error: unrecognized XmlTokenStream token: "+token);
    }

    protected int _nextToken() throws IOException {
        try {
            return _xmlTokens.next();
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsParseException(e, this);
        } catch (IllegalStateException e) {
            // 08-Apr-2021, tatu: Should improve on this, wrt better information
            //   on issue.
            throw new JsonParseException(this, e.getMessage(), e);
        }
    }

    protected void _skipEndElement() throws IOException {
        try {
            _xmlTokens.skipEndElement();
        } catch (XMLStreamException e) {
            StaxUtil.throwAsParseException(e, this);
        } catch (Exception e) {
            throw new JsonParseException(this, e.getMessage(), e);
        }
    }
}
