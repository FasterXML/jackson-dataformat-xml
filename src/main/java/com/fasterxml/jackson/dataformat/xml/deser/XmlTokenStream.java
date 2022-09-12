package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.stream.*;

import com.fasterxml.jackson.dataformat.xml.XmlNameProcessor;
import org.codehaus.stax2.XMLStreamLocation2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.ri.Stax2ReaderAdapter;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.io.ContentReference;

/**
 * Simple helper class used on top of STAX {@link XMLStreamReader} to further
 * abstract out all irrelevant details, and to expose equivalent of flat token
 * stream with no "fluff" tokens (comments, processing instructions, mixed
 * content) all of which is just to simplify
 * actual higher-level conversion to JSON tokens.
 *<p>
 * Beyond initial idea there are also couple of other detours like ability
 * to "replay" some tokens, add virtual wrappers (ironically to support "unwrapped"
 * array values), and to unroll "Objects" into String values in some cases.
 */
public class XmlTokenStream
{
    // // // main token states:

    public final static int XML_START_ELEMENT = 1;
    public final static int XML_END_ELEMENT = 2;
    public final static int XML_ATTRIBUTE_NAME = 3;
    public final static int XML_ATTRIBUTE_VALUE = 4;
    public final static int XML_TEXT = 5;

    // New in 2.12: needed to "re-process" previously encountered START_ELEMENT,
    // with possible leading text
    // public final static int XML_DELAYED_START_ELEMENT = 6;

    // 2.12 also exposes "root scalars" as-is, instead of wrapping as Objects; this
    // needs some more state management too
    public final static int XML_ROOT_TEXT = 7;

    public final static int XML_END = 8;

    // // // token replay states

    private final static int REPLAY_START_DUP = 1;
    private final static int REPLAY_END = 2;
    private final static int REPLAY_START_DELAYED = 3;

    // Some helpful XML Constants

    private final static String XSI_NAMESPACE = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    protected final XMLStreamReader2 _xmlReader;

    // @since 2.13 (was untyped before)
    protected final ContentReference _sourceReference;

    /**
     * Bit flag composed of bits that indicate which
     * {@link FromXmlParser.Feature}s
     * are enabled.
     */
    protected int _formatFeatures;

    protected boolean _cfgProcessXsiNil;

    protected XmlNameProcessor _nameProcessor;

    /*
    /**********************************************************************
    /* Parsing state
    /**********************************************************************
     */

    protected int _currentState;

    protected int _attributeCount;

    /**
     * Marker used to indicate presence of `xsi:nil="true"' in current START_ELEMENT.
     *
     * @since 2.10
     */
    protected boolean _xsiNilFound;

    /**
     * Flag set true if current event is {@code XML_TEXT} and there is START_ELEMENT
     *
     * @since 2.12
     */
    protected boolean _startElementAfterText;

    /**
     * Index of the next attribute of the current START_ELEMENT
     * to return (as field name and value pair), if any; -1
     * when no attributes to return
     */
    protected int _nextAttributeIndex;

    protected String _localName;

    protected String _namespaceURI;

    /**
     * Current text value for TEXT_VALUE returned
     */
    protected String _textValue;

    /**
     * Marker flag set if caller wants to "push back" current token so
     * that next call to {@link #next()} should simply be given what was
     * already read.
     *
     * @since 2.12
     */
    protected boolean _repeatCurrentToken;

    /**
     * Reusable internal value object
     *
     * @since 2.14
     */
    protected XmlNameProcessor.XmlName _nameToDecode = new XmlNameProcessor.XmlName();

    /*
    /**********************************************************************
    /* State for handling virtual wrapping
    /**********************************************************************
     */
    
    /**
     * Flag used to indicate that given element should be "replayed".
     */
    protected int _repeatElement;

    /**
     * Wrapping state, if any active (null if none)
     */
    protected ElementWrapper _currentWrapper;

    /**
     * In cases where we need to 'inject' a virtual END_ELEMENT, we may also
     * need to restore START_ELEMENT afterwards; if so, this is where names
     * are held.
     */
    protected String _nextLocalName;
    protected String _nextNamespaceURI;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public XmlTokenStream(XMLStreamReader xmlReader, ContentReference sourceRef,
            int formatFeatures, XmlNameProcessor nameProcessor)
    {
        _sourceReference = sourceRef;
        _formatFeatures = formatFeatures;
        _cfgProcessXsiNil = FromXmlParser.Feature.PROCESS_XSI_NIL.enabledIn(_formatFeatures);
        _xmlReader = Stax2ReaderAdapter.wrapIfNecessary(xmlReader);
        _nameProcessor = nameProcessor;
    }

    /**
     * Second part of initialization, to be called immediately after construction
     *
     * @since 2.12
     */
    public int initialize() throws XMLStreamException
    {
        // Let's ensure we point to START_ELEMENT...
        if (_xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalArgumentException("Invalid XMLStreamReader passed: should be pointing to START_ELEMENT ("
                    +XMLStreamConstants.START_ELEMENT+"), instead got "+_xmlReader.getEventType());
        }
        _checkXsiAttributes(); // sets _attributeCount, _nextAttributeIndex
        _decodeElementName(_xmlReader.getNamespaceURI(), _xmlReader.getLocalName());

        // 02-Jul-2020, tatu: Two choices: if child elements OR attributes, expose
        //    as Object value; otherwise expose as Text
        // 06-Sep-2022, tatu: Actually expose as Object in almost every situation
        //    as of 2.14: otherwise we have lots of issues with empty POJOs,
        //    Lists, Maps
        if (_xmlReader.isEmptyElement()
            && FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.enabledIn(_formatFeatures)
            && !_xsiNilFound
            && _attributeCount < 1) {
            // 06-Sep-2022, tatu: In fact the only special case of null conversion
            //    of the root empty element
            _textValue = null;
            _startElementAfterText = false;
            return (_currentState = XML_ROOT_TEXT);
        }
        return (_currentState = XML_START_ELEMENT);

        // 06-Sep-2022, tatu: This code was used in 2.12, 2.13, may be
        //   removed after 2.14 if/when no longer needed

        // copied from START_ELEMENT section of _next():
        /*
        final String text = _collectUntilTag();
        if (text == null) {
            // 30-Nov-2020, tatu: [dataformat-xml#435], this is tricky
            //   situation since we got coerced `null`... but at least for
            //   now will have to report as "root String" (... with null contents)
            _textValue = null;
            _startElementAfterText = false;
            return (_currentState = XML_ROOT_TEXT);
        }

        final boolean startElementNext = _xmlReader.getEventType() == XMLStreamReader.START_ELEMENT;
        // If we have no/all-whitespace text followed by START_ELEMENT, ignore text
        if (startElementNext) {
            if (_allWs(text)) {
                _textValue = null;
                return (_currentState = XML_DELAYED_START_ELEMENT);
            }
            _textValue = text;
            return (_currentState = XML_DELAYED_START_ELEMENT);
        }
        _startElementAfterText = false;
        _textValue = text;
        return (_currentState = XML_ROOT_TEXT);
        */
    }

    public XMLStreamReader2 getXmlReader() {
        return _xmlReader;
    }

    /**
     * @since 2.9
     */
    protected void setFormatFeatures(int f) {
        _formatFeatures = f;
        _cfgProcessXsiNil = FromXmlParser.Feature.PROCESS_XSI_NIL.enabledIn(f);
    }

    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    // DEBUGGING
    /*
    public int next() throws XMLStreamException 
    {
        int n = next0();
        switch (n) {
        case XML_START_ELEMENT: 
            System.out.printf(" XmlTokenStream.next(): XML_START_ELEMENT '%s' %s\n", _localName, _loc());
            break;
        case XML_DELAYED_START_ELEMENT: 
            System.out.printf(" XmlTokenStream.next(): XML_DELAYED_START_ELEMENT '%s' %s\n", _localName, _loc());
            break;
        case XML_END_ELEMENT: 
            // 24-May-2020, tatu: no name available for end element so do not print
            System.out.printf(" XmlTokenStream.next(): XML_END_ELEMENT %s\n", _loc());
            break;
        case XML_ATTRIBUTE_NAME: 
            System.out.printf(" XmlTokenStream.next(): XML_ATTRIBUTE_NAME '%s' %s\n", _localName, _loc());
            break;
        case XML_ATTRIBUTE_VALUE: 
            System.out.printf(" XmlTokenStream.next(): XML_ATTRIBUTE_VALUE '%s' %s\n", _textValue, _loc());
            break;
        case XML_TEXT: 
            System.out.printf(" XmlTokenStream.next(): XML_TEXT '%s' %s\n", _textValue, _loc());
            break;
        case XML_END: 
            System.out.printf(" XmlTokenStream.next(): XML_END %s\n", _loc());
            break;
        default:
            throw new IllegalStateException();
        }
        return n;
    }

    private String _loc() {
        JsonLocation loc = getCurrentLocation();
        return String.format("[line: %d, column: %d]", loc.getLineNr(), loc.getColumnNr());
    }
    */

//    public int next0() throws XMLStreamException
    public int next() throws XMLStreamException
    {
        if (_repeatCurrentToken) {
            _repeatCurrentToken = false;
            return _currentState;
        }
        if (_repeatElement != 0) {
            return (_currentState = _handleRepeatElement());
        }
        return _next();
    }

    public void skipEndElement() throws IOException, XMLStreamException
    {
        int type = next();
        if (type != XML_END_ELEMENT) {
            throw new IOException(String.format(
                    "Internal error: Expected END_ELEMENT, got event of type %s",
                    _stateDesc(type)));
        }
    }

    public int getCurrentToken() { return _currentState; }

    public String getText() { return _textValue; }

    /**
     * Accessor for local name of current named event (that is,
     * {@code XML_START_ELEMENT} or {@code XML_ATTRIBUTE_NAME}).
     *<p>
     * NOTE: name NOT accessible on {@code XML_END_ELEMENT}
     */
    public String getLocalName() { return _localName; }

    public String getNamespaceURI() { return _namespaceURI; }

    public boolean hasXsiNil() {
        return _xsiNilFound;
    }

    /*// not used as of 2.10
    public boolean hasAttributes() {
        return (_currentState == XML_START_ELEMENT) && (_attributeCount > 0);
    }
    */

    public void closeCompletely() throws XMLStreamException {
        _xmlReader.closeCompletely();
    }

    public void close() throws XMLStreamException {
        _xmlReader.close();
    }

    public JsonLocation getCurrentLocation() {
        return _extractLocation(_xmlReader.getLocationInfo().getCurrentLocation());
    }

    public JsonLocation getTokenLocation() {
        return _extractLocation(_xmlReader.getLocationInfo().getStartLocation());
    }

    /*
    /**********************************************************************
    /* Internal API: more esoteric methods
    /**********************************************************************
     */
    
    /**
     * Method used to add virtual wrapping, which just duplicates START_ELEMENT
     * stream points to, and its matching closing element.
     */
    protected void repeatStartElement()
    {
//System.out.println(" XmlTokenStream.repeatStartElement() for <"+_localName+">, _currentWrapper was: "+_currentWrapper);
        // sanity check: can only be used when just returned START_ELEMENT:
        if (_currentState != XML_START_ELEMENT) {
            // 14-May-2020, tatu: Looks like we DO end up here with empty Lists; if so,
            //    should NOT actually wrap.
            if (_currentState == XML_END_ELEMENT) {
                return;
            }
            throw new IllegalStateException("Current state not XML_START_ELEMENT but "+_currentStateDesc());
        }
        // Important: add wrapper, to keep track...
        if (_currentWrapper == null) {
            _currentWrapper = ElementWrapper.matchingWrapper(null, _localName, _namespaceURI);
        } else {
            _currentWrapper = ElementWrapper.matchingWrapper(_currentWrapper.getParent(), _localName, _namespaceURI);
        }
//System.out.println(" repeatStartElement for "+_localName+", _currentWrapper now: "+_currentWrapper);
        _repeatElement = REPLAY_START_DUP;
    }

    /**
     * Method that can be called to ask stream to literally just return current token
     * with the next call to {@link #next()}, without more work.
     *
     * @since 2.12
     */
    protected void pushbackCurrentToken()
    {
        _repeatCurrentToken = true;
    }

    /**
     * Method called to skip any attributes current START_ELEMENT may have,
     * so that they are not returned as token.
     * 
     * @since 2.1
     */
    protected void skipAttributes()
    {
//System.out.println(" XmlTokenStream.skipAttributes(), state: "+_currentStateDesc());
        switch (_currentState) {
        case XML_ATTRIBUTE_NAME:
            _attributeCount = 0;
            _currentState = XML_START_ELEMENT;
            break;
        case XML_START_ELEMENT:
            // 06-Jan-2012, tatu: As per [#47] it looks like we should NOT do anything
            //   in this particular case, because it occurs when original element had
            //   no attributes and we now point to the first child element.
//              _attributeCount = 0;
            break;
        case XML_TEXT:
            break; // nothing to do... is it even legal?

            /*
        case XML_DELAYED_START_ELEMENT:
            // 03-Jul-2020, tatu: and here nothing to do either... ?
            break;
            */
        default:
            throw new IllegalStateException(
"Current state not XML_START_ELEMENT or XML_ATTRIBUTE_NAME but "+_currentStateDesc());
        }
    }

    /*
    /**********************************************************************
    /* Internal methods, parsing
    /**********************************************************************
     */

    private final int _next() throws XMLStreamException
    {
//System.out.println(" XmlTokenStream._next(), state: "+_currentStateDesc());
        switch (_currentState) {
        case XML_ATTRIBUTE_VALUE:
            ++_nextAttributeIndex;
            // fall through
        case XML_START_ELEMENT: // attributes to return?
            // 06-Sep-2019, tatu: `xsi:nil` to induce "real" null value?
            if (_xsiNilFound) {
                _xsiNilFound = false;
                // 08-Jul-2021, tatu: as per [dataformat-xml#467] just skip anything
                //   element might have, no need to ensure it was empty
                _xmlReader.skipElement();
                return _handleEndElement();
            }
            if (_nextAttributeIndex < _attributeCount) {
//System.out.println(" XmlTokenStream._next(): Got attr(s)!");
                _decodeAttributeName(_xmlReader.getAttributeNamespace(_nextAttributeIndex),
                        _xmlReader.getAttributeLocalName(_nextAttributeIndex));
                _textValue = _xmlReader.getAttributeValue(_nextAttributeIndex);
                return (_currentState = XML_ATTRIBUTE_NAME);
            }
            // otherwise need to find START/END_ELEMENT or text
            String text = _collectUntilTag();
//System.out.println(" XmlTokenStream._next(): _collectUntilTag -> '"+text+"'");
            final boolean startElementNext = _xmlReader.getEventType() == XMLStreamReader.START_ELEMENT;
//System.out.println(" XmlTokenStream._next(): startElementNext? "+startElementNext);
            // If we have no/all-whitespace text followed by START_ELEMENT, ignore text
            if (startElementNext) {
                if (_allWs(text)) {
                    _startElementAfterText = false;
                    return _initStartElement();
                }
                _startElementAfterText = true;
                _textValue = text;
                
                return (_currentState = XML_TEXT);
            }
            // For END_ELEMENT we will return text, if any
            if (text != null) {
                _startElementAfterText = false;
                _textValue = text;
                return (_currentState = XML_TEXT);
            }
            _startElementAfterText = false;
            return _handleEndElement();

            /*
        case XML_DELAYED_START_ELEMENT: // since 2.12, to support scalar Root Value
            // Two cases: either "simple" with not text
           if (_textValue == null) {
               return _initStartElement();
           }
           // or one where there is first text (to translate into "":<text> key/value entry)
           // then followed by start element
           _startElementAfterText = true;
           return (_currentState = XML_TEXT);
           */

        case XML_ATTRIBUTE_NAME:
            // if we just returned name, will need to just send value next
            return (_currentState = XML_ATTRIBUTE_VALUE);
        case XML_TEXT:
            // mixed text with other elements
            if (_startElementAfterText) {
                _startElementAfterText = false;
                return _initStartElement();
            }
            // text followed by END_ELEMENT
            return _handleEndElement();
        case XML_ROOT_TEXT:
            close();
            return (_currentState = XML_END);
        case XML_END:
            return XML_END;
//            throw new IllegalStateException("No more XML tokens available (end of input)");
        }
        // Ok: must be END_ELEMENT; see what tag we get (or end)
        switch (_skipAndCollectTextUntilTag()) {
        case XMLStreamConstants.END_DOCUMENT:
            close();
            return (_currentState = XML_END);
        case XMLStreamConstants.END_ELEMENT:
            // 24-May-2020, tatu: Need to see if we have "mixed content" to offer
            if (!_allWs(_textValue)) {
                // _textValue already set
                return (_currentState = XML_TEXT);
            }
            return _handleEndElement();
        }
        // 24-May-2020, tatu: Need to see if we have "mixed content" to offer
        if (!_allWs(_textValue)) {
            // _textValue already set
            _startElementAfterText = true;
            return (_currentState = XML_TEXT);
        }

        // START_ELEMENT...
        return _initStartElement();
    }

    /**
     * @return Collected text, if any, EXCEPT that if {@code FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL}
     *    AND empty element, returns {@code null}
     */
    private final String _collectUntilTag() throws XMLStreamException
    {
        // 21-Jun-2017, tatu: Whether exposed as `null` or "" is now configurable...
        if (_xmlReader.isEmptyElement()) {
            _xmlReader.next();
            if (FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.enabledIn(_formatFeatures)) {
                return null;
            }
            return "";
        }

        CharSequence chars = null;
        while (true) {
            switch (_xmlReader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                return (chars == null) ? "" : chars.toString();

            case XMLStreamConstants.END_ELEMENT:
            case XMLStreamConstants.END_DOCUMENT:
                return (chars == null) ? "" : chars.toString();

            // note: SPACE is ignorable (and seldom seen), not to be included
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
                // 17-Jul-2017, tatu: as per [dataformat-xml#236], need to try to...
                {
                    String str = _getText(_xmlReader);
                    if (chars == null) {
                        chars = str;
                    } else  {
                        if (chars instanceof String) {
                            chars = new StringBuilder(chars);
                        }
                        ((StringBuilder)chars).append(str);
                    }
                }
                break;
            default:
                // any other type (proc instr, comment etc) is just ignored
            }
        }
    }

    // Called to skip tokens until start/end tag (or end-of-document) found, but
    // also collecting cdata until then, if any found, for possible "mixed content"
    // to report
    //
    // @since 2.12
    private final int _skipAndCollectTextUntilTag() throws XMLStreamException
    {
        CharSequence chars = null;

        while (_xmlReader.hasNext()) {
            int type;
            switch (type = _xmlReader.next()) {
            case XMLStreamConstants.START_ELEMENT:
            case XMLStreamConstants.END_ELEMENT:
            case XMLStreamConstants.END_DOCUMENT:
                _textValue = (chars == null) ? "" : chars.toString();
                return type;
            // note: SPACE is ignorable (and seldom seen), not to be included
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
                {
                    String str = _getText(_xmlReader);
                    if (chars == null) {
                        chars = str;
                    } else  {
                        if (chars instanceof String) {
                            chars = new StringBuilder(chars);
                        }
                        ((StringBuilder)chars).append(str);
                    }
                }
                break;
            default:
                // any other type (proc instr, comment etc) is just ignored
            }
        }
        throw new IllegalStateException("Expected to find a tag, instead reached end of input");
    }

    private final String _getText(XMLStreamReader2 r) throws XMLStreamException
    {
        try {
            return r.getText();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XMLStreamException) {
                throw (XMLStreamException) cause;
            }
            throw e;
        }
    }

    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */

    private final int _initStartElement() throws XMLStreamException
    {
        final String ns = _xmlReader.getNamespaceURI();
        final String localName = _xmlReader.getLocalName();

        _checkXsiAttributes();

        // Support for virtual wrapping: in wrapping, may either create a new
        // wrapper scope (if in sub-tree, or matches wrapper element itself),
        // or implicitly close existing scope.

        if (_currentWrapper != null) {
            if (_currentWrapper.matchesWrapper(localName, ns)) {
                _currentWrapper = _currentWrapper.intermediateWrapper();
//System.out.println(" _initStartElement(): START_ELEMENT ("+localName+") DOES match ["+_currentWrapper+"]: leave/add intermediate");
            } else {
                // implicit end is more interesting:
//System.out.println(" _initStartElement(): START_ELEMENT ("+localName+") not matching '"+_localName+"'; add extra XML-END-ELEMENT!");
                _localName = _currentWrapper.getWrapperLocalName();
                _namespaceURI = _currentWrapper.getWrapperNamespace();
                _currentWrapper = _currentWrapper.getParent();
                // Important! We also need to restore the START_ELEMENT, so:
                _nextLocalName = localName;
                _nextNamespaceURI = ns;
                _repeatElement = REPLAY_START_DELAYED;
                return (_currentState = XML_END_ELEMENT);
            }
        }
        _decodeElementName(ns, localName);
        return (_currentState = XML_START_ELEMENT);
    }

    /**
     * @since 2.10
     */
    private final void _checkXsiAttributes() {
        int count = _xmlReader.getAttributeCount();
        _attributeCount = count;

        // [dataformat-xml#354]: xsi:nul handling; at first only if first attribute
        if (count >= 1) {
            // [dataformat-xml#468]: may disable xsi:nil processing
            if (_cfgProcessXsiNil
                     && "nil".equals(_xmlReader.getAttributeLocalName(0))) {
                if (XSI_NAMESPACE.equals(_xmlReader.getAttributeNamespace(0))) {
                    // need to skip, regardless of value
                    _nextAttributeIndex = 1;
                    // but only mark as nil marker if enabled
                    _xsiNilFound = "true".equals(_xmlReader.getAttributeValue(0));
                    return;
                }
            }
        }

        _nextAttributeIndex = 0;
        _xsiNilFound = false;
    }

    /**
     * @since 2.14
     */
    protected void _decodeElementName(String namespaceURI, String localName) {
        _nameToDecode.namespace = namespaceURI;
        _nameToDecode.localPart = localName;
        _nameProcessor.decodeName(_nameToDecode);
        _namespaceURI = _nameToDecode.namespace;
        _localName = _nameToDecode.localPart;
    }

    /**
     * @since 2.14
     */
    protected void _decodeAttributeName(String namespaceURI, String localName) {
        _nameToDecode.namespace = namespaceURI;
        _nameToDecode.localPart = localName;
        _nameProcessor.decodeName(_nameToDecode);
        _namespaceURI = _nameToDecode.namespace;
        _localName = _nameToDecode.localPart;
    }

    /**
     * Method called to handle details of repeating "virtual"
     * start/end elements, needed for handling 'unwrapped' lists.
     */
    protected int _handleRepeatElement() throws XMLStreamException 
    {
//System.out.println(" XMLTokenStream._handleRepeatElement()");

        int type = _repeatElement;
        _repeatElement = 0;
        if (type == REPLAY_START_DUP) {
//System.out.println(" XMLTokenStream._handleRepeatElement() for START_ELEMENT: "+_localName+" ("+_xmlReader.getLocalName()+")");
            // important: add the virtual element second time, but not with name to match
            _currentWrapper = _currentWrapper.intermediateWrapper(); // lgtm [java/dereferenced-value-may-be-null]
            return XML_START_ELEMENT;
        }
        if (type == REPLAY_END) {
//System.out.println(" XMLTokenStream._handleRepeatElement() for END_ELEMENT: "+_localName+" ("+_xmlReader.getLocalName()+")");
            _decodeElementName(_xmlReader.getNamespaceURI(), _xmlReader.getLocalName());
            if (_currentWrapper != null) {
                _currentWrapper = _currentWrapper.getParent();
            }
            return XML_END_ELEMENT;
        }
        if (type == REPLAY_START_DELAYED) {
            if (_currentWrapper != null) {
                _currentWrapper = _currentWrapper.intermediateWrapper();
            }
            _decodeElementName(_nextNamespaceURI, _nextLocalName);
            _nextLocalName = null;
            _nextNamespaceURI = null;

//System.out.println(" XMLTokenStream._handleRepeatElement() for START_DELAYED: "+_localName+" ("+_xmlReader.getLocalName()+")");

            return XML_START_ELEMENT;
        }
        throw new IllegalStateException("Unrecognized type to repeat: "+type);
    }
    
    private final int _handleEndElement()
    {
//System.out.println(" XMLTokenStream._handleEndElement()");
        if (_currentWrapper != null) {
            ElementWrapper w = _currentWrapper;
            // important: if we close the scope, must duplicate END_ELEMENT as well
            if (w.isMatching()) {
                _repeatElement = REPLAY_END;
                // 11-Sep-2022, tatu: I _think_ these are already properly decoded
                _localName = w.getWrapperLocalName();
                _namespaceURI = w.getWrapperNamespace();
                _currentWrapper = _currentWrapper.getParent();
//System.out.println(" XMLTokenStream._handleEndElement(): IMPLICIT requestRepeat of END_ELEMENT '"+_localName);
            } else {
                _currentWrapper = _currentWrapper.getParent();
                // 23-May-2020, tatu: Let's clear _localName since it's value is unlikely
                //    to be correct and we may or may not be able to get real one (for
                //    END_ELEMENT could) -- FromXmlParser does NOT use this info
                _localName = "";
                _namespaceURI = "";

            }
        } else {
            // Not (necessarily) known, as per above, so:
            _localName = "";
            _namespaceURI = "";
        }
        return (_currentState = XML_END_ELEMENT);
    }

    private JsonLocation _extractLocation(XMLStreamLocation2 location)
    {
        if (location == null) { // just for impls that might pass null...
            return new JsonLocation(_sourceReference, -1, -1, -1);
        }
        return new JsonLocation(_sourceReference,
                location.getCharacterOffset(),
                location.getLineNumber(),
                location.getColumnNumber());
    }

    protected static boolean _allWs(String str)
    {
        final int len = (str == null) ? 0 : str.length();
        if (len > 0) {
            for (int i = 0; i < len; ++i) {
                if (str.charAt(i) > ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    protected String _currentStateDesc() {
        return _stateDesc(_currentState);
    }

    protected String _stateDesc(int state) {
        switch (state) {
        case XML_START_ELEMENT:
            return "XML_START_ELEMENT";
        case XML_END_ELEMENT:
            return "XML_END_ELEMENT";
        case XML_ATTRIBUTE_NAME:
            return "XML_ATTRIBUTE_NAME";
        case XML_ATTRIBUTE_VALUE:
            return "XML_ATTRIBUTE_VALUE";
        case XML_TEXT:
            return "XML_TEXT";
        // case XML_DELAYED_START_ELEMENT:
        //    return "XML_START_ELEMENT_DELAYED";
        case XML_ROOT_TEXT:
            return "XML_ROOT_TEXT";
        case XML_END:
            return "XML_END";
        }
        return "N/A ("+_currentState+")";
    }

    // for DEBUGGING
    /*
    @Override
    public String toString()
    {
        return String.format("(Token stream: state=%s attr=%s nextAttr=%s"
                +" name=%s text=%s repeat?=%s wrapper=[%s] repeatElement=%s nextName=%s)",
                _currentState, _attributeCount, _nextAttributeIndex,
                _localName, _textValue, _repeatElement, _currentWrapper, _repeatElement, _nextLocalName);
    }
    */
}
