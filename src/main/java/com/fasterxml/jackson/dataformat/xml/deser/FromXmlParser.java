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
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
     * Enumeration that defines all togglable features for XML parsers.
     */
    public enum Feature implements FormatFeature
    {
        /**
         * Feature that indicates whether XML Empty elements (ones where there are
         * no separate start and end tages, but just one tag that ends with "/>")
         * are exposed as {@link JsonToken#VALUE_NULL}) or not. If they are not
         * returned as `null` tokens, they will be returned as {@link JsonToken#VALUE_STRING}
         * tokens with textual value of "" (empty String).
         *<p>
         * Default setting is `true` for backwards compatibility.
         *
         * @since 2.9
         */
        EMPTY_ELEMENT_AS_NULL(true)
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
     * may be changed for interoperability reasons: JAXB, for example, uses
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

    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    final protected IOContext _ioContext;

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

    protected Set<String> _namesToWrap;

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
    /* Life-cycle
    /**********************************************************
     */

    public FromXmlParser(IOContext ctxt, int genericParserFeatures, int xmlFeatures,
            ObjectCodec codec, XMLStreamReader xmlReader)
    {
        super(genericParserFeatures);
        _formatFeatures = xmlFeatures;
        _ioContext = ctxt;
        _objectCodec = codec;
        _parsingContext = XmlReadContext.createRootContext(-1, -1);
        // and thereby start a scope
        _nextToken = JsonToken.START_OBJECT;
        _xmlTokens = new XmlTokenStream(xmlReader, ctxt.getSourceReference(),
                _formatFeatures);
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
     * @since 2.1
     */
    public void addVirtualWrapping(Set<String> namesToWrap)
    {
        /* 17-Sep-2012, tatu: Not 100% sure why, but this is necessary to avoid
         *   problems with Lists-in-Lists properties
         */
        String name = _xmlTokens.getLocalName();
        if (name != null && namesToWrap.contains(name)) {
            _xmlTokens.repeatStartElement();
        }
        _namesToWrap = namesToWrap;
        _parsingContext.setNamesToWrap(namesToWrap);
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
//System.out.println(" isExpectedArrayStart: OBJ->Array, wraps now: "+_parsingContext.getNamesToWrap());
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
//System.out.println(" isExpectedArrayStart?: t="+t);
        return (t == JsonToken.START_ARRAY);
    }

    // DEBUGGING
    /*
    @Override
    public JsonToken nextToken() throws IOException
    {
        JsonToken t = nextToken0();
        if (t != null) {
            switch (t) {
            case FIELD_NAME:
                System.out.println("JsonToken: FIELD_NAME '"+_parsingContext.getCurrentName()+"'");
                break;
            case VALUE_STRING:
                System.out.println("JsonToken: VALUE_STRING '"+getText()+"'");
                break;
            default:
                System.out.println("JsonToken: "+t);
            }
        }
        return t;
    }

//    public JsonToken nextToken0() throws IOException
 */

    
    @Override
    public JsonToken nextToken() throws IOException
    {
        _binaryValue = null;
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
                _namesToWrap = _parsingContext.getNamesToWrap();
                break;
            case FIELD_NAME:
                _parsingContext.setCurrentName(_xmlTokens.getLocalName());
                break;
            default: // VALUE_STRING, VALUE_NULL
                // should be fine as is?
            }
            return t;
        }
        int token;
        try {
            token = _xmlTokens.next();
        } catch (XMLStreamException e) {
            token = StaxUtil.throwAsParseException(e, this);
        }
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
                try {
                    token = _xmlTokens.next();
                } catch (XMLStreamException e) {
                    StaxUtil.throwAsParseException(e, this);
                }
                _mayBeLeaf = true;
                continue;
            }
            String name = _xmlTokens.getLocalName();
            _parsingContext.setCurrentName(name);

            // Ok: virtual wrapping can be done by simply repeating current START_ELEMENT.
            // Couple of ways to do it; but start by making _xmlTokens replay the thing...
            if (_namesToWrap != null && _namesToWrap.contains(name)) {
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
                    return (_currToken = JsonToken.VALUE_NULL);
                }
                _currToken = _parsingContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
                _parsingContext = _parsingContext.getParent();
                _namesToWrap = _parsingContext.getNamesToWrap();
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
                return (_currToken = JsonToken.VALUE_STRING);
            case XmlTokenStream.XML_TEXT:
                _currText = _xmlTokens.getText();
                if (_mayBeLeaf) {
                    _mayBeLeaf = false;
                    // One more refinement (pronunced like "hack") is that if
                    // we had an empty String (or all white space), and we are
                    // deserializing an array, we better hide the empty text.
                    // Also: must skip following END_ELEMENT
                    try {
                        _xmlTokens.skipEndElement();
                    } catch (XMLStreamException e) {
                        StaxUtil.throwAsParseException(e, this);
                    }
                    if (_parsingContext.inArray()) {
                        if (_isEmpty(_currText)) {
                            // 06-Jan-2015, tatu: as per [dataformat-xml#180], need to
                            //    expose as empty Object, not null (or, worse, as used to
                            //    be done, by swallowing the token)
                            _nextToken = JsonToken.END_OBJECT;
                            _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                            return (_currToken = JsonToken.START_OBJECT);
                        }
                    }
                    return (_currToken = JsonToken.VALUE_STRING);
                } else {
                    // [dataformat-xml#177]: empty text may also need to be skipped
                    // but... [dataformat-xml#191]: looks like we can't short-cut, must
                    // loop over again
                    if (_parsingContext.inObject()) {
                        if ((_currToken != JsonToken.FIELD_NAME) && _isEmpty(_currText)) {
                            try {
                                token = _xmlTokens.next();
                            } catch (XMLStreamException e) {
                                StaxUtil.throwAsParseException(e, this);
                            }
                            continue;
                        }
                    }
                }
                // If not a leaf (or otherwise ignorable), need to transform into property...
                _parsingContext.setCurrentName(_cfgNameForTextElement);
                _nextToken = JsonToken.VALUE_STRING;
                return (_currToken = JsonToken.FIELD_NAME);
            case XmlTokenStream.XML_END:
                return (_currToken = null);
            }
        }
    }

    /*
    /**********************************************************
    /* Overrides of specialized nextXxx() methods
    /**********************************************************
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
                return _currText;
            }
            _updateState(t);
            return null;
        }

        int token;

        try {
            token = _xmlTokens.next();
        } catch (XMLStreamException e) {
            token = StaxUtil.throwAsParseException(e, this);
        }

        // mostly copied from 'nextToken()'
        while (token == XmlTokenStream.XML_START_ELEMENT) {
            if (_mayBeLeaf) {
                _nextToken = JsonToken.FIELD_NAME;
                _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
                _currToken = JsonToken.START_OBJECT;
                return null;
            }
            if (_parsingContext.inArray()) {
                try {
                    token = _xmlTokens.next();
                } catch (XMLStreamException e) {
                    StaxUtil.throwAsParseException(e, this);
                }
                _mayBeLeaf = true;
                continue;
            }
            String name = _xmlTokens.getLocalName();
            _parsingContext.setCurrentName(name);
            if (_namesToWrap != null && _namesToWrap.contains(name)) {
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
                return (_currText = "");
            }
            _currToken = _parsingContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
            _parsingContext = _parsingContext.getParent();
            _namesToWrap = _parsingContext.getNamesToWrap();
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
            return (_currText = _xmlTokens.getText());
        case XmlTokenStream.XML_TEXT:
            _currText = _xmlTokens.getText();
            if (_mayBeLeaf) {
                _mayBeLeaf = false;
                // Also: must skip following END_ELEMENT
                try {
                    _xmlTokens.skipEndElement();
                } catch (XMLStreamException e) {
                    StaxUtil.throwAsParseException(e, this);
                }
                // NOTE: this is different from nextToken() -- NO work-around
                // for otherwise empty List/array
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
            _namesToWrap = _parsingContext.getNamesToWrap();
            break;
        case FIELD_NAME:
            _parsingContext.setCurrentName(_xmlTokens.getLocalName());
            break;
        default:
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

    // @since 2.1
    @Override
    public final String getValueAsString() throws IOException {
        return getValueAsString(null);
    }

    @Override
    public String getValueAsString(String defValue) throws IOException
    {
        JsonToken t = _currToken;
        if (t == null) {
            return null;
        }
        switch (t) {
        case FIELD_NAME:
            return getCurrentName();
        case VALUE_STRING:
            return _currText;
        case START_OBJECT:
            // the interesting case; may be able to convert certain kinds of
            // elements (specifically, ones with attributes, CDATA only content)
            // into VALUE_STRING
            try {
                String str = _xmlTokens.convertToString();
                if (str != null) {
                    // need to convert token, as well as "undo" START_OBJECT
                    // note: Should NOT update context, because we will still be getting
                    // matching END_OBJECT, which will undo contexts properly
                    _parsingContext = _parsingContext.getParent();
                    _namesToWrap = _parsingContext.getNamesToWrap();
                    _currToken = JsonToken.VALUE_STRING;
                    _nextToken = null;
                    // One more thing: must explicitly skip the END_OBJECT that would follow
                    try {
                        _xmlTokens.skipEndElement();
                    } catch (XMLStreamException e) {
                        StaxUtil.throwAsParseException(e, this);
                    }
                    return (_currText = str);
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwAsParseException(e, this);
            }
            return null;
        default:
            if (_currToken.isScalarValue()) {
                return _currToken.asString();
            }
        }
        return defValue;
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
    /* Numeric accessors
    /**********************************************************
     */

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getDoubleValue() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getFloatValue() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIntValue() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLongValue() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public NumberType getNumberType() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Number getNumberValue() throws IOException {
        // TODO Auto-generated method stub
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
                    _parsingContext.getStartLocation(_ioContext.getSourceReference())),
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

    protected boolean _isEmpty(String str)
    {
        int len = (str == null) ? 0 : str.length();
        if (len > 0) {
            for (int i = 0; i < len; ++i) {
                if (str.charAt(i) > ' ') {
                    return false;
                }
            }
        }
        return true;
    }
}
