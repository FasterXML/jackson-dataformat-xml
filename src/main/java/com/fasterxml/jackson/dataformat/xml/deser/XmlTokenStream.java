package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import javax.xml.stream.*;

import org.codehaus.stax2.XMLStreamLocation2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.ri.Stax2ReaderAdapter;

import com.fasterxml.jackson.core.JsonLocation;

/**
 * Simple helper class used on top of STAX {@link XMLStreamReader} to further
 * abstract out all irrelevant details, and to expose equivalent of flat token
 * stream with no "fluff" tokens (comments, processing instructions, mixed
 * content) all of which is just to simplify
 * actual higher-level conversion to JSON tokens
 */
public class XmlTokenStream
{
    // // // main token states:
    
    public final static int XML_START_ELEMENT = 1;
    public final static int XML_END_ELEMENT = 2;
    public final static int XML_ATTRIBUTE_NAME = 3;
    public final static int XML_ATTRIBUTE_VALUE = 4;
    public final static int XML_TEXT = 5;
    public final static int XML_END = 6;

    // // // token replay states

    private final static int REPLAY_START_DUP = 1;
    private final static int REPLAY_END = 2;
    private final static int REPLAY_START_DELAYED = 3;
    
    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    final protected XMLStreamReader2 _xmlReader;

    final protected Object _sourceReference;

    /**
     * Bit flag composed of bits that indicate which
     * {@link FromXmlParser.Feature}s
     * are enabled.
     */
    protected int _formatFeatures;
    
    /*
    /**********************************************************************
    /* Parsing state
    /**********************************************************************
     */

    protected int _currentState;

    protected int _attributeCount;

    /**
     * If true we have a START_ELEMENT with mixed text
     *
     * @since 2.8
     */
    protected boolean _mixedText;

    /**
     * Index of the next attribute of the current START_ELEMENT
     * to return (as field name and value pair), if any; -1
     * when no attributes to return
     */
    protected int _nextAttributeIndex = 0;

    protected String _localName;

    protected String _namespaceURI;

    protected String _textValue;
    
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

    public XmlTokenStream(XMLStreamReader xmlReader, Object sourceRef,
            int formatFeatures)
    {
        _sourceReference = sourceRef;
        // Let's ensure we point to START_ELEMENT...
        if (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalArgumentException("Invalid XMLStreamReader passed: should be pointing to START_ELEMENT ("
                    +XMLStreamConstants.START_ELEMENT+"), instead got "+xmlReader.getEventType());
        }
        _xmlReader = Stax2ReaderAdapter.wrapIfNecessary(xmlReader);
        _currentState = XML_START_ELEMENT;
        _localName = _xmlReader.getLocalName();
        _namespaceURI = _xmlReader.getNamespaceURI();
        _attributeCount = _xmlReader.getAttributeCount();
        _formatFeatures = formatFeatures;
    }

    public XMLStreamReader2 getXmlReader() {
        return _xmlReader;
    }

    /**
     * @since 2.9
     */
    protected void setFormatFeatures(int f) {
        _formatFeatures = f;
    }
    
    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    // DEBUGGING
    /*
    public int next() throws IOException 
    {
        int n = next0();
        switch (n) {
        case XML_START_ELEMENT: 
            System.out.println(" XML-token: XML_START_ELEMENT '"+_localName+"'");
            break;
        case XML_END_ELEMENT: 
            System.out.println(" XML-token: XML_END_ELEMENT '"+_localName+"'");
            break;
        case XML_ATTRIBUTE_NAME: 
            System.out.println(" XML-token: XML_ATTRIBUTE_NAME '"+_localName+"'");
            break;
        case XML_ATTRIBUTE_VALUE: 
            System.out.println(" XML-token: XML_ATTRIBUTE_VALUE '"+_textValue+"'");
            break;
        case XML_TEXT: 
            System.out.println(" XML-token: XML_TEXT '"+_textValue+"'");
            break;
        case XML_END: 
            System.out.println(" XML-token: XML_END");
            break;
        default:
            throw new IllegalStateException();
        }
        return n;
    }
    */

    public int next() throws XMLStreamException 
    {
        if (_repeatElement != 0) {
            return (_currentState = _handleRepeatElement());
        }
        return _next();
    }

    public void skipEndElement() throws IOException, XMLStreamException
    {
        int type = next();
        if (type != XML_END_ELEMENT) {
            throw new IOException("Expected END_ELEMENT, got event of type "+type);
        }
    }

    public int getCurrentToken() { return _currentState; }

    public String getText() { return _textValue; }
    public String getLocalName() { return _localName; }
    public String getNamespaceURI() { return _namespaceURI; }
    public boolean hasAttributes() {
        return (_currentState == XML_START_ELEMENT) && (_attributeCount > 0);
    }
    
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
     * 
     * @since 2.1
     */
    protected void repeatStartElement()
    {
//System.out.println(" -> repeatStartElement for "+_localName);        
        // sanity check: can only be used when just returned START_ELEMENT:
        if (_currentState != XML_START_ELEMENT) {
            throw new IllegalStateException("Current state not XML_START_ELEMENT ("
                    +XML_START_ELEMENT+") but "+_currentState);
        }
        // Important: add wrapper, to keep track...
        if (_currentWrapper == null) {
            _currentWrapper = ElementWrapper.matchingWrapper(_currentWrapper, _localName, _namespaceURI);
        } else {
            _currentWrapper = ElementWrapper.matchingWrapper(_currentWrapper.getParent(), _localName, _namespaceURI);
        }
        _repeatElement = REPLAY_START_DUP;
    }

    /**
     * Method called to skip any attributes current START_ELEMENT may have,
     * so that they are not returned as token.
     * 
     * @since 2.1
     */
    protected void skipAttributes()
    {
        if (_currentState == XML_ATTRIBUTE_NAME) {
            _attributeCount = 0;
            _currentState = XML_START_ELEMENT;
        } else if (_currentState == XML_START_ELEMENT) {
            /* 06-Jan-2012, tatu: As per [#47] it looks like we should NOT do anything
             *   in this particular case, because it occurs when original element had
             *   no attributes and we now point to the first child element.
             */
//              _attributeCount = 0;
        } else if (_currentState == XML_TEXT) {
            ; // nothing to do... is it even legal?
        } else {
            throw new IllegalStateException("Current state not XML_START_ELEMENT or XML_ATTRIBUTE_NAME ("
                    +XML_START_ELEMENT+") but "+_currentState);
        }
    }

    protected String convertToString() throws XMLStreamException
    {
        // only applicable to cases where START_OBJECT was induced by attributes
        if (_currentState != XML_ATTRIBUTE_NAME || _nextAttributeIndex != 0) {
            return null;
        }
        String text = _collectUntilTag();
        // 23-Dec-2015, tatu: Used to require text not to be null, but as per
        //   [dataformat-xml#167], empty tag does count
        if (_xmlReader.getEventType() == XMLStreamReader.END_ELEMENT) {
            if (text == null) {
                text = "";
            }
            if (_currentWrapper != null) {
                _currentWrapper = _currentWrapper.getParent();
            }
            // just for diagnostics, reset to element name (from first attribute name)
            _localName = _xmlReader.getLocalName();
            _namespaceURI = _xmlReader.getNamespaceURI();
            _attributeCount = 0;
            _currentState = XML_TEXT;
            _textValue = text;
            return text;
        }
        // Anything to do in failed case? Roll back whatever we found or.. ?
        return null;
    }

    /*
    /**********************************************************************
    /* Internal methods, parsing
    /**********************************************************************
     */

    private final int _next() throws XMLStreamException
    {
        switch (_currentState) {
        case XML_ATTRIBUTE_VALUE:
            ++_nextAttributeIndex;
            // fall through
        case XML_START_ELEMENT: // attributes to return?
            if (_nextAttributeIndex < _attributeCount) {
                _localName = _xmlReader.getAttributeLocalName(_nextAttributeIndex);
                _namespaceURI = _xmlReader.getAttributeNamespace(_nextAttributeIndex);
                _textValue = _xmlReader.getAttributeValue(_nextAttributeIndex);
                return (_currentState = XML_ATTRIBUTE_NAME);
            }
            // otherwise need to find START/END_ELEMENT or text
            String text = _collectUntilTag();
            final boolean startElementNext = _xmlReader.getEventType() == XMLStreamReader.START_ELEMENT;
            // If we have no/all-whitespace text followed by START_ELEMENT, ignore text
            if (startElementNext) {
                if (text == null || _allWs(text)) {
                    _mixedText = false;
                    return _initStartElement();
                }
                _mixedText = true;
                _textValue = text;
                return (_currentState = XML_TEXT);
            }
            // For END_ELEMENT we will return text, if any
            if (text != null) {
                _mixedText = false;
                _textValue = text;
                return (_currentState = XML_TEXT);
            }
            _mixedText = false;
            return _handleEndElement();

        case XML_ATTRIBUTE_NAME:
            // if we just returned name, will need to just send value next
            return (_currentState = XML_ATTRIBUTE_VALUE);
        case XML_TEXT:
            // mixed text with other elements
            if (_mixedText){
                _mixedText = false;
                return _initStartElement();
            }
            // text followed by END_ELEMENT
            return _handleEndElement();
        case XML_END:
            return XML_END;
//            throw new IllegalStateException("No more XML tokens available (end of input)");
        }

        // Ok: must be END_ELEMENT; see what tag we get (or end)
        switch (_skipUntilTag()) {
        case XMLStreamConstants.END_DOCUMENT:
            return (_currentState = XML_END);
        case XMLStreamConstants.END_ELEMENT:
            return _handleEndElement();
        }
        // START_ELEMENT...
        return _initStartElement();
    }

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
                    return chars == null ? "" : chars.toString();

                case XMLStreamConstants.END_ELEMENT:
                case XMLStreamConstants.END_DOCUMENT:
                    // 04-May-2018, tatu: For 3.0 we can actually start exposing <tag></tag> ALSO
                    //    as `null`, as long as we default `String` null handling to coerce that to
                    //    "empty"
                    if (chars == null) {
                        if (FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.enabledIn(_formatFeatures)) {
                            return null;
                        }
                        return "";
                    }
                    return chars.toString();

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

    private final int _skipUntilTag() throws XMLStreamException
    {
        while (_xmlReader.hasNext()) {
            int type;
            switch (type = _xmlReader.next()) {
            case XMLStreamConstants.START_ELEMENT:
            case XMLStreamConstants.END_ELEMENT:
            case XMLStreamConstants.END_DOCUMENT:
                return type;
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
        _attributeCount = _xmlReader.getAttributeCount();
        _nextAttributeIndex = 0;

        /* Support for virtual wrapping: in wrapping, may either
         * create a new wrapper scope (if in sub-tree, or matches
         * wrapper element itself), or implicitly close existing
         * scope.
         */
        if (_currentWrapper != null) {
            if (_currentWrapper.matchesWrapper(localName, ns)) {
                _currentWrapper = _currentWrapper.intermediateWrapper();
            } else {
                // implicit end is more interesting:
                _localName = _currentWrapper.getWrapperLocalName();
                _namespaceURI = _currentWrapper.getWrapperNamespace();
                _currentWrapper = _currentWrapper.getParent();
//System.out.println(" START_ELEMENT ("+localName+") not matching '"+_localName+"'; add extra XML-END-ELEMENT!");
                // Important! We also need to restore the START_ELEMENT, so:
                _nextLocalName = localName;
                _nextNamespaceURI = ns;
                _repeatElement = REPLAY_START_DELAYED;
                return (_currentState = XML_END_ELEMENT);
            }
        }
        _localName = localName;
        _namespaceURI = ns;
        return (_currentState = XML_START_ELEMENT);
    }

    /**
     * Method called to handle details of repeating "virtual"
     * start/end elements, needed for handling 'unwrapped' lists.
     */
    protected int _handleRepeatElement() throws XMLStreamException 
    {
        int type = _repeatElement;
        _repeatElement = 0;
        if (type == REPLAY_START_DUP) {
//System.out.println("handleRepeat for START_ELEMENT: "+_localName+" ("+_xmlReader.getLocalName()+")");
            // important: add the virtual element second time, but not with name to match
            _currentWrapper = _currentWrapper.intermediateWrapper();
            return XML_START_ELEMENT;
        }
        if (type == REPLAY_END) {
//System.out.println("handleRepeat for END_ELEMENT: "+_localName+" ("+_xmlReader.getLocalName()+")");
            _localName = _xmlReader.getLocalName();
            _namespaceURI = _xmlReader.getNamespaceURI();
            if (_currentWrapper != null) {
                _currentWrapper = _currentWrapper.getParent();
            }
            return XML_END_ELEMENT;
        }
        if (type == REPLAY_START_DELAYED) {
            if (_currentWrapper != null) {
                _currentWrapper = _currentWrapper.intermediateWrapper();
            }
            _localName = _nextLocalName;
            _namespaceURI = _nextNamespaceURI;
            _nextLocalName = null;
            _nextNamespaceURI = null;
            
//System.out.println("handleRepeat for START_DELAYED: "+_localName+" ("+_xmlReader.getLocalName()+")");

            return XML_START_ELEMENT;
        }
        throw new IllegalStateException("Unrecognized type to repeat: "+type);
    }
    
    private final int _handleEndElement()
    {
        if (_currentWrapper != null) {
            ElementWrapper w = _currentWrapper;
            // important: if we close the scope, must duplicate END_ELEMENT as well
            if (w.isMatching()) {
                _repeatElement = REPLAY_END;
                _localName = w.getWrapperLocalName();
                _namespaceURI = w.getWrapperNamespace();
                _currentWrapper = _currentWrapper.getParent();
//System.out.println(" IMPLICIT requestRepeat of END_ELEMENT '"+_localName);
            } else {
                _currentWrapper = _currentWrapper.getParent();
            }
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


    protected boolean _allWs(String str)
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

    // for DEBUGGING
    @Override
    public String toString()
    {
        return String.format("(Token stream: state=%s attr=%s nextAttr=%s"
                +" name=%s text=%s repeat?=%s wrapper=[%s] repeatElement=%s nextName=%s)",
                _currentState, _attributeCount, _nextAttributeIndex,
                _localName, _textValue, _repeatElement, _currentWrapper, _repeatElement, _nextLocalName);
    }
}
