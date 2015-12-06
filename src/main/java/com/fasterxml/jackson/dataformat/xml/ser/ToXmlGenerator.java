package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.Stax2WriterAdapter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * {@link JsonGenerator} that outputs JAXB-style XML output instead of JSON content.
 * Operation requires calling code (usually either standard Jackson serializers,
 * or in some cases (like <code>BeanSerializer</code>) customised ones) to do
 * additional configuration calls beyond regular {@link JsonGenerator} API,
 * mostly to pass namespace information.
 */
public final class ToXmlGenerator
    extends GeneratorBase
{
    private final static String NEWLINE = System.getProperty("line.separator", "\n");

    /**
     * If we support optional definition of element names, this is the element
     * name to use...
     */
    protected final static String DEFAULT_UNKNOWN_ELEMENT = "unknown";
    
    /**
     * Enumeration that defines all togglable extra XML-specific features
     */
    public enum Feature implements FormatFeature
    {
        /**
         * Feature that controls whether XML declaration should be written before
         * when generator is initialized (true) or not (false)
         */
        WRITE_XML_DECLARATION(false),

        /**
         * Feature that controls whether output should be done as XML 1.1; if so,
         * certain aspects may differ from default (1.0) processing: for example,
         * XML declaration will be automatically added (regardless of setting
         * <code>WRITE_XML_DECLARATION</code>) as this is required for reader to
         * know to use 1.1 compliant handling. XML 1.1 can be used to allow quoted
         * control characters (Ascii codes 0 through 31) as well as additional linefeeds
         * and name characters.
         */
        WRITE_XML_1_1(false)
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

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    final protected XMLStreamWriter2 _xmlWriter;

    final protected XMLStreamWriter _originalXmlWriter;
    
    /**
     * Marker flag set if the underlying stream writer has to emulate
     * Stax2 API: this is problematic if trying to use {@link #writeRaw} calls.
     */
    final protected boolean _stax2Emulation;
    
    final protected IOContext _ioContext;

    /**
     * Bit flag composed of bits that indicate which
     * {@link ToXmlGenerator.Feature}s
     * are enabled.
     */
    protected int _xmlFeatures;

    /**
     * We may need to use XML-specific indentation as well
     */
    protected XmlPrettyPrinter _xmlPrettyPrinter;
    
    /*
    /**********************************************************
    /* XML Output state
    /**********************************************************
     */

    /**
     * Marker set when {@link #initGenerator()} has been called or not.
     * 
     * @since 2.2
     */
    protected boolean _initialized;
    
    /**
     * Element or attribute name to use for next output call.
     * Assigned by either code that initiates serialization
     * or bean serializer.
     */
    protected QName _nextName = null;

    /**
     * Marker flag that indicates whether next name to write
     * implies an attribute (true) or element (false)
     */
    protected boolean _nextIsAttribute = false;

    /**
     * Marker flag used to indicate that the next write of a (property)
     * value should be done without using surrounding start/end
     * elements. Flag is to be cleared once unwrapping has been triggered
     * once.
     */
    protected boolean _nextIsUnwrapped = false;

    /**
     * Marker flag used to indicate that the next write of a (property)
     * value should be as CData
     */
    protected boolean _nextIsCData = false;
    
    /**
     * To support proper serialization of arrays it is necessary to keep
     * stack of element names, so that we can "revert" to earlier 
     */
    protected LinkedList<QName> _elementNameStack = new LinkedList<QName>();
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public ToXmlGenerator(IOContext ctxt, int genericGeneratorFeatures, int xmlFeatures,
            ObjectCodec codec, XMLStreamWriter sw)
    {
        super(genericGeneratorFeatures, codec);
        _xmlFeatures = xmlFeatures;
        _ioContext = ctxt;
        _originalXmlWriter = sw;
        _xmlWriter = Stax2WriterAdapter.wrapIfNecessary(sw);
        _stax2Emulation = (_xmlWriter != sw);
        _xmlPrettyPrinter = (_cfgPrettyPrinter instanceof XmlPrettyPrinter) ?
        		(XmlPrettyPrinter) _cfgPrettyPrinter : null;
    }

    /**
     * Method called before writing any other output, to optionally
     * output XML declaration.
     */
    public void initGenerator()  throws IOException
    {
        if (_initialized) {
            return;
        }
        _initialized = true;
        try {
            if ((_xmlFeatures & Feature.WRITE_XML_1_1.getMask()) != 0) {
                _xmlWriter.writeStartDocument("UTF-8", "1.1");
                if (_xmlPrettyPrinter != null) {
                  _xmlWriter.writeRaw(NEWLINE);
                }
            } else if ((_xmlFeatures & Feature.WRITE_XML_DECLARATION.getMask()) != 0) {
                _xmlWriter.writeStartDocument("UTF-8", "1.0");
                if (_xmlPrettyPrinter != null) {
                  _xmlWriter.writeRaw(NEWLINE);
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    /*
    /**********************************************************
    /* Overridden methods, configuration
    /**********************************************************
     */

    /**
     * Standard JSON indenter does not work well with XML, use
     * default XML indenter instead.
     *<p>
     * !!! TODO: same as implementation in core 2.6; override may be
     * removed from 2.7
     */
    @Override
    public final JsonGenerator useDefaultPrettyPrinter()
    {
        // related to [dataformat-xml#136], need to verify:
        if (_cfgPrettyPrinter != null) {
            return this;
        }
        return setPrettyPrinter(_constructDefaultPrettyPrinter());
    }

    @Override
    protected PrettyPrinter _constructDefaultPrettyPrinter() {
        return new DefaultXmlPrettyPrinter();
    }

    @Override
    public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
        _cfgPrettyPrinter = pp;
        _xmlPrettyPrinter = (pp instanceof XmlPrettyPrinter) ?
               (XmlPrettyPrinter) pp : null;
        return this;
    }

    @Override
    public Object getOutputTarget() {
        // Stax2 does not expose underlying target, so best we can do is to return
        // the Stax XMLStreamWriter instance:
        return _originalXmlWriter;
    }

    /**
     * Stax2 does not expose buffered content amount, so we can only return
     * <code>-1</code> from here
     */
    @Override
    public int getOutputBuffered() {
        return -1;
    }

    /*
    /**********************************************************
    /* Extended API, configuration
    /**********************************************************
     */

    public ToXmlGenerator enable(Feature f) {
        _xmlFeatures |= f.getMask();
        return this;
    }

    public ToXmlGenerator disable(Feature f) {
        _xmlFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(Feature f) {
        return (_xmlFeatures & f.getMask()) != 0;
    }

    public ToXmlGenerator configure(Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
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
    public XMLStreamWriter getStaxWriter() {
        return _xmlWriter;
    }
    
    /*
    /**********************************************************
    /* Extended API, passing XML specific settings
    /**********************************************************
     */

    public void setNextIsAttribute(boolean isAttribute)
    {
        _nextIsAttribute = isAttribute;
    }

    public void setNextIsUnwrapped(boolean isUnwrapped)
    {
        _nextIsUnwrapped = isUnwrapped;
    }

    public void setNextIsCData(boolean isCData)
    {
        _nextIsCData = isCData;
    }
    
    public final void setNextName(QName name)
    {
        _nextName = name;
    }

    /**
     * Method that does same as {@link #setNextName}, unless
     * a name has already been set.
     * 
     * @since 2.1.2
     */
    public final boolean setNextNameIfMissing(QName name)
    {
        if (_nextName == null) {
            _nextName = name;
            return true;
        }
        return false;
    }
    
    /**
     * Methdod called when a structured (collection, array, map) is being
     * output.
     * 
     * @param wrapperName Element used as wrapper around elements, if any (null if none)
     * @param wrappedName Element used around individual content items (can not
     *   be null)
     */
    public void startWrappedValue(QName wrapperName, QName wrappedName) throws IOException
    {
        if (wrapperName != null) {
            try {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeStartElement(_xmlWriter,
                            wrapperName.getNamespaceURI(), wrapperName.getLocalPart());
                } else {
                    _xmlWriter.writeStartElement(wrapperName.getNamespaceURI(), wrapperName.getLocalPart());
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwXmlAsIOException(e);
            }
        }
        this.setNextName(wrappedName);
    }

    /**
     * Method called after a structured collection output has completed
     */
    public void finishWrappedValue(QName wrapperName, QName wrappedName) throws IOException
    {
        // First: wrapper to close?
        if (wrapperName != null) {
            try {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeEndElement(_xmlWriter, _writeContext.getEntryCount());
                } else {
                    _xmlWriter.writeEndElement();
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwXmlAsIOException(e);
            }
        }
    }

    /**
     * Trivial helper method called when to add a replicated wrapper name
     * 
     * @since 2.2
     */
    public void writeRepeatedFieldName() throws IOException
    {
        if (_writeContext.writeFieldName(_nextName.getLocalPart()) == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
    }
    
    /*
    /**********************************************************
    /* JsonGenerator method overrides
    /**********************************************************
     */
    
    /* Most overrides in this section are just to make methods final,
     * to allow better inlining...
     */

    @Override
    public final void writeFieldName(String name)  throws IOException
    {
        if (_writeContext.writeFieldName(name) == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        // Should this ever get called?
        String ns = (_nextName == null) ? "" : _nextName.getNamespaceURI();
        setNextName(new QName(ns, name));
    }
    
    @Override
    public final void writeStringField(String fieldName, String value) throws IOException
    {
        writeFieldName(fieldName);
        writeString(value);
    }
    
    /*
    /**********************************************************
    /* JsonGenerator output method implementations, structural
    /**********************************************************
     */

    @Override
    public final void writeStartArray() throws IOException
    {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartArray(this);
        } else {
            // nothing to do here; no-operation
        }
    }
    
    @Override
    public final void writeEndArray() throws IOException
    {
        if (!_writeContext.inArray()) {
            _reportError("Current context not an ARRAY but "+_writeContext.getTypeDesc());
        }
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndArray(this, _writeContext.getEntryCount());
        } else {
            // nothing to do here; no-operation
        }
        _writeContext = _writeContext.getParent();
    }

    @Override
    public final void writeStartObject() throws IOException
    {
        _verifyValueWrite("start an object");
        _writeContext = _writeContext.createChildObjectContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartObject(this);
        } else {
            _handleStartObject();
        }
    }

    @Override
    public final void writeEndObject() throws IOException
    {
        if (!_writeContext.inObject()) {
            _reportError("Current context not an object but "+_writeContext.getTypeDesc());
        }
        _writeContext = _writeContext.getParent();
        if (_cfgPrettyPrinter != null) {
            // as per [Issue#45], need to suppress indentation if only attributes written:
            int count = _nextIsAttribute ? 0 : _writeContext.getEntryCount();
            _cfgPrettyPrinter.writeEndObject(this, count);
        } else {
            _handleEndObject();
        }
    }

    // note: public just because pretty printer needs to make a callback
    public final void _handleStartObject() throws IOException
    {
        if (_nextName == null) {
            handleMissingName();
        }
        // Need to keep track of names to make Lists work correctly
        _elementNameStack.addLast(_nextName);
        try {
            _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }
    
    // note: public just because pretty printer needs to make a callback
    public final void _handleEndObject() throws IOException
    {
        // We may want to repeat same element, so:
        if (_elementNameStack.isEmpty()) {
            throw new JsonGenerationException("Can not write END_ELEMENT without open START_ELEMENT");
        }
        _nextName = _elementNameStack.removeLast();
        try {
            // note: since attributes don't nest, can only have one attribute active, so:
            _nextIsAttribute = false;
            _xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }
    
    /*
    /**********************************************************
    /* Output method implementations, textual
    /**********************************************************
     */

    @Override
    public void writeFieldName(SerializableString name) throws IOException
    {
        writeFieldName(name.getValue());
    }
    
    @Override
    public void writeString(String text) throws IOException
    {
        _verifyValueWrite("write String value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) { // must write attribute name and value with one call
                _xmlWriter.writeAttribute(_nextName.getNamespaceURI(), _nextName.getLocalPart(), text);
            } else if (checkNextIsUnwrapped()) {
                // [Issue#56] Should figure out how to prevent indentation for end element
                //   but for now, let's just make sure structure is correct
                //if (_xmlPrettyPrinter != null) { ... }
                if(_nextIsCData) {
                    _xmlWriter.writeCData(text);
                } else {
                    _xmlWriter.writeCharacters(text);
                }
            } else if (_xmlPrettyPrinter != null) {
                _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                        _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                        text, _nextIsCData);
            } else {
                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                if(_nextIsCData) {
                    _xmlWriter.writeCData(text);
                } else {
                    _xmlWriter.writeCharacters(text);
                }
                _xmlWriter.writeEndElement();
            } 
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }    
    
    @Override
    public void writeString(char[] text, int offset, int len) throws IOException
    {
        _verifyValueWrite("write String value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeAttribute(_nextName.getNamespaceURI(), _nextName.getLocalPart(), new String(text, offset, len));
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                if(_nextIsCData) {
                    _xmlWriter.writeCData(text, offset, len);
                } else {
                    _xmlWriter.writeCharacters(text, offset, len);
                }
            } else if (_xmlPrettyPrinter != null) {
                _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                        _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                        text, offset, len, _nextIsCData);
            } else {
                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                if(_nextIsCData) {
                    _xmlWriter.writeCData(text, offset, len);
                } else {
                    _xmlWriter.writeCharacters(text, offset, len);
                }
                _xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeString(SerializableString text) throws IOException {
        writeString(text.getValue());
    }
    
    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException
    {
        // could add support for this case if we really want it (and can make Stax2 support it)
        _reportUnsupportedOperation();
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException
    {
        // could add support for this case if we really want it (and can make Stax2 support it)
        _reportUnsupportedOperation();
    }

    /*
    /**********************************************************
    /* Output method implementations, unprocessed ("raw")
    /**********************************************************
     */

    @Override
    public void writeRaw(String text) throws IOException
    {
        // [Issue#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRaw");
        }
        try {
            _xmlWriter.writeRaw(text);
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException
    {
        // [Issue#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRaw");
        }
        try {
            _xmlWriter.writeRaw(text, offset, len);
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException
    {
        // [Issue#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRaw");
        }
        try {
            _xmlWriter.writeRaw(text, offset, len);
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeRaw(char c) throws IOException
    {
        writeRaw(String.valueOf(c));
    }
    
    /*
    /**********************************************************
    /* Output method implementations, base64-encoded binary
    /**********************************************************
     */

    @Override
    public void writeBinary(Base64Variant b64variant,
    		byte[] data, int offset, int len) throws IOException
    {
        if (data == null) {
            writeNull();
            return;
        }
        _verifyValueWrite("write Binary value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                // Stax2 API only has 'full buffer' write method:
                byte[] fullBuffer = toFullBuffer(data, offset, len);
                _xmlWriter.writeBinaryAttribute("", _nextName.getNamespaceURI(), _nextName.getLocalPart(), fullBuffer);
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                _xmlWriter.writeBinary(data, offset, len);
            } else {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                            _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                            data, offset, len);
                } else {
                    _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                    _xmlWriter.writeBinary(data, offset, len);
                    _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    private byte[] toFullBuffer(byte[] data, int offset, int len)
    {
        // might already be ok:
        if (offset == 0 && len == data.length) {
            return data;
        }
        byte[] result = new byte[len];
        if (len > 0) {
            System.arraycopy(data, offset, result, 0, len);
        }
        return result;
    }
    
    /*
    /**********************************************************
    /* Output method implementations, primitive
    /**********************************************************
     */

    @Override
    public void writeBoolean(boolean value) throws IOException
    {
        _verifyValueWrite("write boolean value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeBooleanAttribute(null, _nextName.getNamespaceURI(), _nextName.getLocalPart(), value);
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                _xmlWriter.writeBoolean(value);
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			value);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                _xmlWriter.writeBoolean(value);
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNull() throws IOException
    {
        _verifyValueWrite("write null value");
        if (_nextName == null) {
            handleMissingName();
        }
        // !!! TODO: proper use of 'xsd:isNil' ?
        try {
            if (_nextIsAttribute) {
                /* With attributes, best just leave it out, right? (since there's no way
                 * to use 'xsi:nil')
                 */
            } else if (checkNextIsUnwrapped()) {
            	// as with above, best left unwritten?
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafNullElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart());
                } else {
	            	_xmlWriter.writeEmptyElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(int i) throws IOException
    {
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeIntAttribute(null, _nextName.getNamespaceURI(), _nextName.getLocalPart(), i);
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                _xmlWriter.writeInt(i);
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			i);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                _xmlWriter.writeInt(i);
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(long l) throws IOException
    {
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeLongAttribute(null, _nextName.getNamespaceURI(), _nextName.getLocalPart(), l);
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeLong(l);
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			l);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                _xmlWriter.writeLong(l);
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(double d) throws IOException
    {
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeDoubleAttribute(null, _nextName.getNamespaceURI(), _nextName.getLocalPart(), d);
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeDouble(d);
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			d);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                _xmlWriter.writeDouble(d);
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(float f) throws IOException
    {
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeFloatAttribute(null, _nextName.getNamespaceURI(), _nextName.getLocalPart(), f);
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeFloat(f);
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			f);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                _xmlWriter.writeFloat(f);
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(BigDecimal dec) throws IOException
    {
        if (dec == null) {
            writeNull();
            return;
        }
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        boolean usePlain = isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        try {
            if (_nextIsAttribute) {
                if (usePlain) {
                    _xmlWriter.writeAttribute("", _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                            dec.toPlainString());
                } else {
                    _xmlWriter.writeDecimalAttribute("", _nextName.getNamespaceURI(), _nextName.getLocalPart(), dec);
                }
            } else if (checkNextIsUnwrapped()) {
                if (usePlain) {
                    _xmlWriter.writeCharacters(dec.toPlainString());
                } else {
                    _xmlWriter.writeDecimal(dec);
                }
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			dec);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                if (usePlain) {
	                    _xmlWriter.writeDecimal(dec);
	                } else {
                         _xmlWriter.writeCharacters(dec.toPlainString());
	                }
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(BigInteger value) throws IOException
    {
        if (value == null) {
            writeNull();
            return;
        }
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeIntegerAttribute("",
                		_nextName.getNamespaceURI(), _nextName.getLocalPart(), value);
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeInteger(value);
            } else {
                if (_xmlPrettyPrinter != null) {
                	_xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                			_nextName.getNamespaceURI(), _nextName.getLocalPart(),
                			value);
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                _xmlWriter.writeInteger(value);
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, UnsupportedOperationException
    {
        writeString(encodedValue);
    }

    /*
    /**********************************************************
    /* Implementations, overrides for other methods
    /**********************************************************
     */
    
    @Override
    protected final void _verifyValueWrite(String typeMsg) throws IOException
    {
        int status = _writeContext.writeValue();
        if (status == JsonWriteContext.STATUS_EXPECT_NAME) {
            _reportError("Can not "+typeMsg+", expecting field name");
        }
    }

    /*
    /**********************************************************
    /* Low-level output handling
    /**********************************************************
     */

    @Override
    public void flush() throws IOException
    {
        if (isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
            try {
                _xmlWriter.flush();
            } catch (XMLStreamException e) {
                StaxUtil.throwXmlAsIOException(e);
            }
        }
    }

    @Override
    public void close() throws IOException
    {
//        boolean wasClosed = _closed;
        super.close();

        /* 05-Dec-2008, tatu: To add [JACKSON-27], need to close open
         *   scopes.
         */
        // First: let's see that we still have buffers...
        if (isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
            try {
                while (true) {
                    JsonStreamContext ctxt = getOutputContext();
                    if (ctxt.inArray()) {
                        writeEndArray();
                    } else if (ctxt.inObject()) {
                        writeEndObject();
                    } else {
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                /* 29-Nov-2010, tatu: Stupid, stupid SJSXP doesn't do array checks, so we get
                 *   hit by this as a collateral problem in some cases. Yuck.
                 */
                throw new JsonGenerationException(e);
            }
        }
        try {
            if (_ioContext.isResourceManaged() || isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
                _xmlWriter.closeCompletely();
            } else {
                _xmlWriter.close();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwXmlAsIOException(e);
        }
    }

    @Override
    protected void _releaseBuffers() {
        // Nothing to do here, as we have no buffers
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Method called to see if unwrapping is required; and if so,
     * clear the flag (so further calls will return 'false' unless
     * state is re-set)
     */
    protected boolean checkNextIsUnwrapped()
    {
        if (_nextIsUnwrapped) {
    		    _nextIsUnwrapped = false;
    		    return true;
        }
        return false;
    }
    
    protected void handleMissingName() {
        throw new IllegalStateException("No element/attribute name specified when trying to output element");
    }

    /**
     * Method called 
     */
    protected void  _reportUnimplementedStax2(String missingMethod) throws IOException
    {
        throw new JsonGenerationException("Underlying Stax XMLStreamWriter (of type "
                +_originalXmlWriter.getClass().getName()
                +") does not implement Stax2 API natively and is missing method '"
                +missingMethod+"': this breaks functionality such as indentation that relies on it. "
                +"You need to upgrade to using compliant Stax implementation like Woodstox or Aalto");
    }
}
