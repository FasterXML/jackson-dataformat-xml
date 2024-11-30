package tools.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamReader2;

import tools.jackson.core.*;
import tools.jackson.core.base.ParserMinimalBase;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.io.NumberInput;
import tools.jackson.core.util.ByteArrayBuilder;
import tools.jackson.core.util.JacksonFeatureSet;
import tools.jackson.dataformat.xml.util.CaseInsensitiveNameSet;
import tools.jackson.dataformat.xml.util.StaxUtil;

import tools.jackson.dataformat.xml.PackageVersion;
import tools.jackson.dataformat.xml.XmlNameProcessor;
import tools.jackson.dataformat.xml.XmlReadFeature;

/**
 * {@link JsonParser} implementation that exposes XML structure as
 * set of JSON events that can be used for data binding.
 */
public class FromXmlParser
    extends ParserMinimalBase
    implements ElementWrappable
{
    /**
     * The default name placeholder for XML text segments is empty
     * String ("").
     */
    public final static String DEFAULT_UNNAMED_TEXT_PROPERTY = "";

    /**
     * XML format has some peculiarities, indicated via capability
     * system.
     */
    protected final static JacksonFeatureSet<StreamReadCapability> XML_READ_CAPABILITIES =
            DEFAULT_READ_CAPABILITIES
                .with(StreamReadCapability.DUPLICATE_PROPERTIES)
                .with(StreamReadCapability.SCALARS_AS_OBJECTS)
                .with(StreamReadCapability.UNTYPED_SCALARS)
            ;

    /**
     * In cases where a start element has both attributes and non-empty textual
     * value, we have to create a bogus property; we will use this as
     * the property name.
     *<p>
     * Name used for pseudo-property used for returning XML Text value (which does
     * not have actual element name to use). Defaults to empty String, but
     * may be changed for inter-operability reasons: JAXB, for example, uses
     * "value" as name.
     */
    protected String _cfgNameForTextElement = DEFAULT_UNNAMED_TEXT_PROPERTY;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link XmlReadFeature}s
     * are enabled.
     */
    protected int _formatFeatures;

    /*
    /**********************************************************************
    /* Parsing state
    /**********************************************************************
     */

    /**
     * Information about parser context, context in which
     * the next token is to be parsed (root, array, object).
     */
    protected XmlReadContext _streamReadContext;

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
     */
    protected boolean _nextIsLeadingMixed;

    /*
    /**********************************************************************
    /* Parsing state, parsed values
    /**********************************************************************
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
    /**********************************************************************
    /* Parsing state, number decoding
    /**********************************************************************
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
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public FromXmlParser(ObjectReadContext readCtxt, IOContext ioCtxt,
            int parserFeatures, int xmlFeatures,
            XMLStreamReader xmlReader,
            XmlNameProcessor nameProcessor)
    {
        super(readCtxt, ioCtxt, parserFeatures);
        _formatFeatures = xmlFeatures;
        _streamReadContext = XmlReadContext.createRootContext(-1, -1);
        _xmlTokens = new XmlTokenStream(xmlReader, ioCtxt.contentReference(),
                    _formatFeatures, nameProcessor);

        final int firstToken;
        try {
            firstToken = _xmlTokens.initialize();
        } catch (XMLStreamException e) {
            StaxUtil.throwAsReadException(e, this);
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

    public void setXMLTextElementName(String name) {
        _cfgNameForTextElement = name;
    }

    @Override // since 3.0
    public XMLStreamReader2 streamReadInputSource() {
        return _xmlTokens.getXmlReader();
    }

    /*
    /**********************************************************************
    /* Overrides: capability introspection methods
    /**********************************************************************
     */

    @Override
    public boolean canReadObjectId() { return false; }

    @Override
    public boolean canReadTypeId() { return false; }

    @Override
    public JacksonFeatureSet<StreamReadCapability> streamReadCapabilities() {
        return XML_READ_CAPABILITIES;
    }

    /*
    /**********************************************************************
    /* Extended API, access to some internal components
    /**********************************************************************
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
    /**********************************************************************
    /* ElementWrappable implementation
    /**********************************************************************
     */


    @Override
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
        if (!_streamReadContext.inRoot()
                 && !_streamReadContext.getParent().inRoot()) {
            String name = _xmlTokens.getLocalName();
            if ((name != null) && namesToWrap.contains(name)) {
//System.out.println("REPEAT from addVirtualWrapping() for '"+name+"'");
                _xmlTokens.repeatStartElement();
            }
        }
        _streamReadContext.setNamesToWrap(namesToWrap);
    }

    /*
    /**********************************************************************
    /* JsonParser impl, closing etc
    /**********************************************************************
     */

    /**
     * Method that can be called to get the name associated with
     * the current event.
     */
    @Override
    public String currentName()
    {
        // start markers require information from parent
        String name;
        if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
            XmlReadContext parent = _streamReadContext.getParent();
            name = parent.currentName();
        } else {
            name = _streamReadContext.currentName();
        }
        // sanity check
        if (name == null) {
            throw new IllegalStateException("Missing name, in state: "+_currToken);
        }
        return name;
    }

    // Basic `close()` from base class works fine
    // public void close() throws JacksonException
    
    @Override
    protected void _closeInput() throws IOException {
        try {
            if (_ioContext.isResourceManaged() || isEnabled(StreamReadFeature.AUTO_CLOSE_SOURCE)) {
                _xmlTokens.closeCompletely();
            } else {
                _xmlTokens.close();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsReadException(e, this);
        }
    }

    @Override
    protected void _releaseBuffers() {
        // anything we can/must release? Underlying parser should do all of it, for now?
    }

    /*
    /**********************************************************************
    /* JsonParser impl, access to current state
    /**********************************************************************
     */
    
    @Override public TokenStreamContext streamReadContext() { return _streamReadContext; }
    @Override public void assignCurrentValue(Object v) { _streamReadContext.assignCurrentValue(v); }
    @Override public Object currentValue() { return _streamReadContext.currentValue(); }

    /**
     * Method that return the <b>starting</b> location of the current
     * token; that is, position of the first character from input
     * that starts the current token.
     */
    @Override
    public TokenStreamLocation currentTokenLocation() {
        return _xmlTokens.getTokenLocation();
    }

    /**
     * Method that returns location of the last processed character;
     * usually for error reporting purposes
     */
    @Override
    public TokenStreamLocation currentLocation() {
        return _xmlTokens.getCurrentLocation();
    }

    /*
    /**********************************************************************
    /* JsonParser impl, traversal
    /**********************************************************************
     */
    
    /**
     * Since xml representation can not really distinguish between array
     * and object starts (both are represented with elements), this method
     * is overridden and taken to mean that expectation is that the current
     * start element is to mean 'start array', instead of default of
     * 'start object'.
     */
    @Override
    public boolean isExpectedStartArrayToken()
    {
        JsonToken t = _currToken;
        if (t == JsonToken.START_OBJECT) {
            _updateToken(JsonToken.START_ARRAY);
            // Ok: must replace current context with array as well
            _streamReadContext.convertToArray();
//System.out.println(" FromXmlParser.isExpectedArrayStart(): OBJ->Array");
            // And just in case a property name was to be returned, wipe it
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
                    _updateToken(JsonToken.VALUE_NUMBER_INT);
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
                            _updateToken(JsonToken.VALUE_NUMBER_INT);
                            return true;
                        }
                    }
                    _numberLong = l;
                    _numTypesValid = NR_LONG;
                    _updateToken(JsonToken.VALUE_NUMBER_INT);
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
                _updateToken(JsonToken.VALUE_NUMBER_INT);
                return true;
            }
        }
        return (t == JsonToken.VALUE_NUMBER_INT);
    }

    // DEBUGGING
    /*
    @Override
    public JsonToken nextToken() throws JacksonException
    {
        JsonToken t = nextToken0();
        if (t != null) {
            final String loc = (_parsingContext == null) ? "NULL" : String.valueOf(_parsingContext.pathAsPointer());
            switch (t) {
            case PROPERTY_NAME:
                System.out.printf("FromXmlParser.nextToken() at '%s': JsonToken.PROPERTY_NAME '%s'\n", loc, _parsingContext.currentName());
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

//    public JsonToken nextToken0() throws JacksonException
    @Override
    public JsonToken nextToken() throws JacksonException
    {
        _binaryValue = null;
        _numTypesValid = NR_UNKNOWN;
//System.out.println("FromXmlParser.nextToken0: _nextToken = "+_nextToken);
        if (_nextToken != null) {
            final JsonToken t = _updateToken(_nextToken);
            _nextToken = null;

            switch (t) {
            case START_OBJECT:
                _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                break;
            case START_ARRAY:
                _streamReadContext = _streamReadContext.createChildArrayContext(-1, -1);
                break;
            case END_OBJECT:
            case END_ARRAY:
                _streamReadContext = _streamReadContext.getParent();
                break;
            case PROPERTY_NAME:
                // 29-Mar-2021, tatu: [dataformat-xml#442]: special case of leading
                //    mixed text added
                if (_nextIsLeadingMixed) {
                    _nextIsLeadingMixed = false;
                    _streamReadContext.setCurrentName(_cfgNameForTextElement);
                    _nextToken = JsonToken.VALUE_STRING;
                } else {
                    _streamReadContext.setCurrentName(_xmlTokens.getLocalName());
                }
                break;
            default: // VALUE_STRING, VALUE_NULL
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index` anyway; not
                //    used for Object contexts, updated automatically by "createChildXxxContext"
                _streamReadContext.valueStarted();
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
                _nextToken = JsonToken.PROPERTY_NAME;
                _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                return _updateToken(JsonToken.START_OBJECT);
            }
            if (_streamReadContext.inArray()) {
                // Yup: in array, so this element could be verified; but it won't be
                // reported anyway, and we need to process following event.
                token = _nextToken();
                _mayBeLeaf = true;
                continue;
            }
            String name = _xmlTokens.getLocalName();
            _streamReadContext.setCurrentName(name);

            // Ok: virtual wrapping can be done by simply repeating current START_ELEMENT.
            // Couple of ways to do it; but start by making _xmlTokens replay the thing...
            if (_streamReadContext.shouldWrap(name)) {
                _xmlTokens.repeatStartElement();
            }

            _mayBeLeaf = true;
            // Ok: in array context we need to skip reporting property names.
            // But what's the best way to find next token?
            return _updateToken(JsonToken.PROPERTY_NAME);
        }

        // Ok; beyond start element, what do we get?
        while (true) {
            switch (token) {
            case XmlTokenStream.XML_END_ELEMENT:
                // Simple, except that if this is a leaf, need to suppress end:
                if (_mayBeLeaf) {
                    _mayBeLeaf = false;
                    if (_streamReadContext.inArray()) {
                        // 06-Jan-2015, tatu: as per [dataformat-xml#180], need to
                        //    expose as empty Object, not null
                        _nextToken = JsonToken.END_OBJECT;
                        _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                        return _updateToken(JsonToken.START_OBJECT);
                    }
                    // 07-Sep-2019, tatu: for [dataformat-xml#353], must NOT return second null
                    if (_currToken != JsonToken.VALUE_NULL) {
                        // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                        _streamReadContext.valueStarted();
                        return _updateToken(JsonToken.VALUE_NULL);
                    }
                }
                _updateToken(_streamReadContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT);
                _streamReadContext = _streamReadContext.getParent();
                return _currToken;

            case XmlTokenStream.XML_ATTRIBUTE_NAME:
                // If there was a chance of leaf node, no more...
                if (_mayBeLeaf) {
                    _mayBeLeaf = false;
                    _nextToken = JsonToken.PROPERTY_NAME;
                    _currText = _xmlTokens.getText();
                    _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                    return _updateToken(JsonToken.START_OBJECT);
                }
                _streamReadContext.setCurrentName(_xmlTokens.getLocalName());
                return _updateToken(JsonToken.PROPERTY_NAME);
            case XmlTokenStream.XML_ATTRIBUTE_VALUE:
                _currText = _xmlTokens.getText();
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _streamReadContext.valueStarted();
                return _updateToken(JsonToken.VALUE_STRING);
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
                        if (_streamReadContext.inArray()) {
                            if (XmlTokenStream._allWs(_currText)) {
                                // 06-Jan-2015, tatu: as per [dataformat-xml#180], need to
                                //    expose as empty Object, not null (or, worse, as used to
                                //    be done, by swallowing the token)
                                _nextToken = JsonToken.END_OBJECT;
                                _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                                return _updateToken(JsonToken.START_OBJECT);
                            }
                        }
                        return _updateToken(JsonToken.VALUE_STRING);
                    }
                    if (token != XmlTokenStream.XML_START_ELEMENT) {
                        throw _constructReadException(String.format(
"Internal error: Expected END_ELEMENT (%d) or START_ELEMENT (%d), got event of type %d",
XmlTokenStream.XML_END_ELEMENT, XmlTokenStream.XML_START_ELEMENT, token));
                    }
                    // fall-through, except must create new context AND push back
                    // START_ELEMENT we just saw:
                    _xmlTokens.pushbackCurrentToken();
                    _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                }
                // [dataformat-xml#177]: empty text may also need to be skipped
                // but... [dataformat-xml#191]: looks like we can't short-cut, must
                // loop over again
                if (_streamReadContext.inObject()) {
                    if (_currToken == JsonToken.PROPERTY_NAME) {
                        // 29-Mar-2021, tatu: [dataformat-xml#442]: need special handling for
                        //    leading mixed content; requires 3-token sequence for which _nextToken
                        //    along is not enough.
                        _nextIsLeadingMixed = true;
                        _nextToken = JsonToken.PROPERTY_NAME;
                        return _updateToken(JsonToken.START_OBJECT);
                    } else if (XmlTokenStream._allWs(_currText)) {
                        token = _nextToken();
                        continue;
                    }
                } else if (_streamReadContext.inArray()) {
                    // [dataformat-xml#319] Aaaaand for Arrays too
                    if (XmlTokenStream._allWs(_currText)) {
                        token = _nextToken();
                        continue;
                    }
                    // 29-Mar-2021, tatu: This seems like an error condition...
                    //   How should we indicate it? As of 2.13, report as unexpected state
                    /*
                    throw _constructReadException(
"Unexpected non-whitespace text ('%s') in Array context: should not occur (or should be handled)",
_currText);
);
                    */

                    // [dataformat-xml#509] 2.13 introduced a defect in which an Exception was thrown above, breaking
                    // parsing of mixed content arrays (https://github.com/FasterXML/jackson-dataformat-xml/issues/509).
                    // This exception case was removed to enable continued support of that functionality, but more
                    // robust state handling may be in order.
                    // See comment https://github.com/FasterXML/jackson-dataformat-xml/pull/604
                }

                // If not a leaf (or otherwise ignorable), need to transform into property...
                _streamReadContext.setCurrentName(_cfgNameForTextElement);
                _nextToken = JsonToken.VALUE_STRING;
                return _updateToken(JsonToken.PROPERTY_NAME);
            case XmlTokenStream.XML_END:
                return _updateTokenToNull();
            default:
                return _internalErrorUnknownToken(token);
            }
        }
    }

    /*
    /**********************************************************************
    /* Overrides of specialized nextXxx() methods
    /**********************************************************************
     */

    /*
    @Override
    public String nextName() throws JacksonException {
        if (nextToken() == JsonToken.PROPERTY_NAME) {
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
    public String nextTextValue() throws JacksonException
    {
        _binaryValue = null;
        if (_nextToken != null) {
            final JsonToken t = _updateToken(_nextToken);
            _nextToken = null;

            // expected case; yes, got a String
            if (t == JsonToken.VALUE_STRING) {
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _streamReadContext.valueStarted();
                return _currText;
            }
            _updateState(t);
            return null;
        }

        int token = _nextToken();

        // mostly copied from 'nextToken()'
        while (token == XmlTokenStream.XML_START_ELEMENT) {
            if (_mayBeLeaf) {
                _nextToken = JsonToken.PROPERTY_NAME;
                _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                _updateToken(JsonToken.START_OBJECT);
                return null;
            }
            if (_streamReadContext.inArray()) {
                token = _nextToken();
                _mayBeLeaf = true;
                continue;
            }
            String name = _xmlTokens.getLocalName();
            _streamReadContext.setCurrentName(name);
            if (_streamReadContext.shouldWrap(name)) {
//System.out.println("REPEAT from nextTextValue()");
                _xmlTokens.repeatStartElement();
            }
            _mayBeLeaf = true;
            _updateToken(JsonToken.PROPERTY_NAME);
            return null;
        }

        // Ok; beyond start element, what do we get?
        switch (token) {
        case XmlTokenStream.XML_END_ELEMENT:
            if (_mayBeLeaf) {
                _mayBeLeaf = false;
                // 18-Mar-2023, tatu: [dataformat-xml#584 / #585] in 2.14 and before
                //    returned VALUE_STRING on assumption we never expose `null`s if
                //    asked text value -- but that seems incorrect. Hoping this won't
                //    break anything in 2.15+

                _updateToken(JsonToken.VALUE_NULL);
                // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
                _streamReadContext.valueStarted();
                return (_currText = null);
            }
           _updateToken(_streamReadContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT);
           _streamReadContext = _streamReadContext.getParent();
            break;
        case XmlTokenStream.XML_ATTRIBUTE_NAME:
            // If there was a chance of leaf node, no more...
            if (_mayBeLeaf) {
                _mayBeLeaf = false;
                _nextToken = JsonToken.PROPERTY_NAME;
                _currText = _xmlTokens.getText();
                _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
                _updateToken(JsonToken.START_OBJECT);
            } else {
                _streamReadContext.setCurrentName(_xmlTokens.getLocalName());
                _updateToken(JsonToken.PROPERTY_NAME);
            }
            break;
        case XmlTokenStream.XML_ATTRIBUTE_VALUE:
            _updateToken(JsonToken.VALUE_STRING);
            // 13-May-2020, tatu: [dataformat-xml#397]: advance `index`
            _streamReadContext.valueStarted();
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
                _streamReadContext.valueStarted();
                _updateToken(JsonToken.VALUE_STRING);
                return _currText;
            }
            // If not a leaf, need to transform into property...
            _streamReadContext.setCurrentName(_cfgNameForTextElement);
            _nextToken = JsonToken.VALUE_STRING;
            _updateToken(JsonToken.PROPERTY_NAME);
            break;
        case XmlTokenStream.XML_END:
            _updateTokenToNull();
        default:
            return _internalErrorUnknownToken(token);
        }
        return null;
    }

    private void _updateState(JsonToken t)
    {
        switch (t) {
        case START_OBJECT:
            _streamReadContext = _streamReadContext.createChildObjectContext(-1, -1);
            break;
        case START_ARRAY:
            _streamReadContext = _streamReadContext.createChildArrayContext(-1, -1);
            break;
        case END_OBJECT:
        case END_ARRAY:
            _streamReadContext = _streamReadContext.getParent();
            break;
        case PROPERTY_NAME:
            _streamReadContext.setCurrentName(_xmlTokens.getLocalName());
            break;
        default:
            _internalErrorUnknownToken(t);
        }
    }

    /*
    /**********************************************************************
    /* Public API, access to token information, text
    /**********************************************************************
     */

    @Override
    public String getText() throws JacksonException
    {
        if (_currToken == null) {
            return null;
        }
        switch (_currToken) {
        case PROPERTY_NAME:
            return currentName();
        case VALUE_STRING:
            return _currText;
        default:
            return _currToken.asString();
        }
    }

    @Override
    public char[] getTextCharacters() throws JacksonException {
        String text = getText();
        return (text == null)  ? null : text.toCharArray();
    }

    @Override
    public int getTextLength() throws JacksonException {
        String text = getText();
        return (text == null)  ? 0 : text.length();
    }

    @Override
    public int getTextOffset() throws JacksonException {
        return 0;
    }

    /**
     * XML input actually would offer access to character arrays; but since
     * we must coalesce things it cannot really be exposed.
     */
    @Override
    public boolean hasTextCharacters() {
        return false;
    }

    @Override
    public int getText(Writer writer) throws JacksonException
    {
        String str = getText();
        if (str == null) {
            return 0;
        }
        try {
            writer.write(str);
        } catch (IOException e) {
            throw _wrapIOFailure(e);
        }
        return str.length();
    }

    /*
    /**********************************************************************
    /* Public API, access to token information, binary
    /**********************************************************************
     */

    @Override
    public Object getEmbeddedObject() throws JacksonException {
        // no way to embed POJOs for now...
        return null;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws JacksonException
    {
        if (_currToken != JsonToken.VALUE_STRING &&
                (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT || _binaryValue == null)) {
            _reportError("Current token ("+_currToken+") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        // To ensure that we won't see inconsistent data, better clear up state...
        if (_binaryValue == null) {
            try {
                _binaryValue = _decodeBase64(b64variant);
            } catch (IllegalArgumentException iae) {
                throw _constructReadException("Failed to decode VALUE_STRING as base64 (%s): %s",
                        b64variant, iae.getMessage());
            }
        }        
        return _binaryValue;
    }

    @SuppressWarnings("resource")
    protected byte[] _decodeBase64(Base64Variant b64variant) throws JacksonException
    {
        ByteArrayBuilder builder = _getByteArrayBuilder();
        final String str = getText();
        _decodeBase64(str, builder, b64variant);
        return builder.toByteArray();
    }

    /*
    /**********************************************************************
    /* Numeric accessors (implemented since 2.12)
    /**********************************************************************
     */

    @Override
    public boolean isNaN() {
        return false; // can't have since we only coerce integers
    }

    @Override
    public NumberType getNumberType() {
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
    public Number getNumberValue() throws JacksonException {
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
    public int getIntValue() throws JacksonException {
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
    public long getLongValue() throws JacksonException {
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
    public BigInteger getBigIntegerValue() throws JacksonException {
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
    public float getFloatValue() throws JacksonException {
        if ((_numTypesValid & NR_FLOAT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_FLOAT);
            }
        }
        return _convertNumberToFloat();
    }

    @Override
    public double getDoubleValue() throws JacksonException {
        if ((_numTypesValid & NR_DOUBLE) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_DOUBLE);
            }
        }
        return _convertNumberToDouble();
    }

    @Override
    public BigDecimal getDecimalValue() throws JacksonException {
        if ((_numTypesValid & NR_BIGDECIMAL) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _checkNumericValue(NR_BIGDECIMAL);
            }
        }
        return _convertNumberToBigDecimal();
    }

    // // // Helper methods for Numeric accessors

    protected final void _checkNumericValue(int expType) throws JacksonException {
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            return;
        }
        throw _constructReadException("Current token (%s) not numeric, can not use numeric value accessors",
                currentToken());
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

    protected void _convertNumberToInt() throws JacksonException
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
                _reportOverflowInt();
            }
            _numberInt = _numberBigInt.intValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_INT;
    }
    
    protected void _convertNumberToLong() throws JacksonException
    {
        if ((_numTypesValid & NR_INT) != 0) {
            _numberLong = (long) _numberInt;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (BI_MIN_LONG.compareTo(_numberBigInt) > 0 
                    || BI_MAX_LONG.compareTo(_numberBigInt) < 0) {
                _reportOverflowLong();
            }
            _numberLong = _numberBigInt.longValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_LONG;
    }
    
    protected void _convertNumberToBigInteger() throws JacksonException
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

    protected float _convertNumberToFloat() throws JacksonException
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
    
    protected double _convertNumberToDouble() throws JacksonException
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

    protected BigDecimal _convertNumberToBigDecimal() throws JacksonException
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
    /**********************************************************************
    /* Abstract method impls for stuff from JsonParser
    /**********************************************************************
     */

    /**
     * Method called when an EOF is encountered between tokens.
     * If so, it may be a legitimate EOF, but only iff there
     * is no open non-root context.
     */
    @Override
    protected void _handleEOF() throws StreamReadException
    {
        if (!_streamReadContext.inRoot()) {
            String marker = _streamReadContext.inArray() ? "Array" : "Object";
            _reportInvalidEOF(String.format(
                    ": expected close marker for %s (start marker at %s)",
                    marker,
                    _streamReadContext.startLocation(_ioContext.contentReference())),
                    null);
        }
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

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

    protected int _nextToken() throws JacksonException {
        try {
            return _xmlTokens.next();
        } catch (XMLStreamException e) {
            return StaxUtil.throwAsReadException(e, this);
        } catch (IllegalStateException e) {
            // 08-Apr-2021, tatu: Should improve on this, wrt better information
            //   on issue.
            throw new StreamReadException(this, e.getMessage(), e);
        }
    }

    protected void _skipEndElement() throws JacksonException {
        try {
            _xmlTokens.skipEndElement();
        } catch (XMLStreamException e) {
            StaxUtil.throwAsReadException(e, this);
        } catch (Exception e) {
            throw new StreamReadException(this, e.getMessage(), e);
        }
    }
}
