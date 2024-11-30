package tools.jackson.dataformat.xml.ser;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.Stax2WriterAdapter;

import tools.jackson.core.*;
import tools.jackson.core.base.GeneratorBase;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.json.DupDetector;
import tools.jackson.core.util.SimpleStreamWriteContext;
import tools.jackson.dataformat.xml.XmlPrettyPrinter;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import tools.jackson.dataformat.xml.util.StaxUtil;
import tools.jackson.core.util.JacksonFeatureSet;

import tools.jackson.dataformat.xml.XmlNameProcessor;
import tools.jackson.dataformat.xml.PackageVersion;

/**
 * {@link JsonGenerator} that outputs JAXB-style XML output instead of JSON content.
 * Operation requires calling code (usually either standard Jackson serializers,
 * or in some cases (like <code>BeanSerializer</code>) customized ones) to do
 * additional configuration calls beyond regular {@link JsonGenerator} API,
 * mostly to pass namespace information.
 */
public class ToXmlGenerator
    extends GeneratorBase
{
    /**
     * If we support optional definition of element names, this is the element
     * name to use...
     */
    protected final static String DEFAULT_UNKNOWN_ELEMENT = "unknown";

    

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    protected final XMLStreamWriter2 _xmlWriter;

    protected final XMLStreamWriter _originalXmlWriter;

    /**
     * We may need to use XML-specific indentation as well
     */
    protected final XmlPrettyPrinter _xmlPrettyPrinter;

    /**
     * Bit flag composed of bits that indicate which
     * {@link XmlWriteFeature}s
     * are enabled.
     */
    protected int _formatFeatures;

    /**
     * Marker flag set if the underlying stream writer has to emulate
     * Stax2 API: this is problematic if trying to use {@link #writeRaw} calls.
     */
    protected final boolean _stax2Emulation;

    /*
    /**********************************************************************
    /* Logical output state
    /**********************************************************************
     */

    /**
     * Object that keeps track of the current contextual state of the generator.
     */
    protected SimpleStreamWriteContext _streamWriteContext;

    /**
     * Escapes names with invalid XML characters
     *
     * @since 2.14
     */
    protected XmlNameProcessor _nameProcessor;

    /*
    /**********************************************************************
    /* XML Output state
    /**********************************************************************
     */

    /**
     * Marker set when {@link #initGenerator()} has been called or not.
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

    /**
     * Reusable internal value object
     */
    protected XmlNameProcessor.XmlName _nameToEncode = new XmlNameProcessor.XmlName();

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public ToXmlGenerator(ObjectWriteContext writeCtxt, IOContext ioCtxt,
            int streamWriteFeatures, int xmlFeatures,
            XMLStreamWriter sw, XmlPrettyPrinter pp, XmlNameProcessor nameProcessor)
    {
        super(writeCtxt, ioCtxt, streamWriteFeatures);
        _formatFeatures = xmlFeatures;
        _originalXmlWriter = sw;
        _xmlWriter = Stax2WriterAdapter.wrapIfNecessary(sw);
        _stax2Emulation = (_xmlWriter != sw);
        _xmlPrettyPrinter = pp;
        final DupDetector dups = StreamWriteFeature.STRICT_DUPLICATE_DETECTION.enabledIn(streamWriteFeatures)
                ? DupDetector.rootDetector(this) : null;
        _streamWriteContext = SimpleStreamWriteContext.createRootContext(dups);
        _nameProcessor = nameProcessor;
    }

    /**
     * Method called before writing any other output, to optionally
     * output XML declaration.
     */
    public void initGenerator() throws JacksonException
    {
        if (_initialized) {
            return;
        }
        _initialized = true;
        try {
            boolean xmlDeclWritten;
            if (XmlWriteFeature.WRITE_XML_1_1.enabledIn(_formatFeatures)) {
                _xmlWriter.writeStartDocument("UTF-8", "1.1");
                xmlDeclWritten = true;
            } else if (XmlWriteFeature.WRITE_XML_DECLARATION.enabledIn(_formatFeatures)) {
                _xmlWriter.writeStartDocument("UTF-8", "1.0");
                xmlDeclWritten = true;
            } else {
                xmlDeclWritten = false;
            }
            // as per [dataformat-xml#172], try adding indentation
            if (xmlDeclWritten && (_xmlPrettyPrinter != null)) {
                // ... but only if it is likely to succeed:
                if (!_stax2Emulation) {
                    _xmlPrettyPrinter.writePrologLinefeed(_xmlWriter);
                }
            }
            if (XmlWriteFeature.AUTO_DETECT_XSI_TYPE.enabledIn(_formatFeatures)) {
                _xmlWriter.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
    }

    /*
    /**********************************************************************
    /* Versioned
    /**********************************************************************
     */

    @Override public Version version() { return PackageVersion.VERSION; }

    /*
    /**********************************************************************
    /* Overridden output state handling methods
    /**********************************************************************
     */
    
    @Override
    public final TokenStreamContext streamWriteContext() { return _streamWriteContext; }

    @Override
    public final Object currentValue() {
        return _streamWriteContext.currentValue();
    }

    @Override
    public final void assignCurrentValue(Object v) {
        _streamWriteContext.assignCurrentValue(v);
    }

    /*
    /**********************************************************************
    /* Overridden methods, configuration
    /**********************************************************************
     */

    @Override
    protected PrettyPrinter _constructDefaultPrettyPrinter() {
        return new DefaultXmlPrettyPrinter();
    }

    @Override
    public Object streamWriteOutputTarget() {
        // Stax2 does not expose underlying target, so best we can do is to return
        // the Stax XMLStreamWriter instance:
        return _originalXmlWriter;
    }

    /**
     * Stax2 does not expose buffered content amount, so we can only return
     * <code>-1</code> from here
     */
    @Override
    public int streamWriteOutputBuffered() {
        return -1;
    }

    /*
    /**********************************************************************
    /* Extended API, configuration
    /**********************************************************************
     */

    public final boolean isEnabled(XmlWriteFeature f) {
        return (_formatFeatures & f.getMask()) != 0;
    }

    public ToXmlGenerator configure(XmlWriteFeature f, boolean state) {
        if (state) {
            _formatFeatures |= f.getMask();
        } else {
            _formatFeatures &= ~f.getMask();
        }
        return this;
    }

    @Override
    public JacksonFeatureSet<StreamWriteCapability> streamWriteCapabilities() {
        return DEFAULT_TEXTUAL_WRITE_CAPABILITIES;
    }

    public boolean inRoot() {
        return _streamWriteContext.inRoot();
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
    public XMLStreamWriter getStaxWriter() {
        return _xmlWriter;
    }
    
    /*
    /**********************************************************************
    /* Extended API, passing XML specific settings
    /**********************************************************************
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
    public void startWrappedValue(QName wrapperName, QName wrappedName) throws JacksonException
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
                StaxUtil.throwAsWriteException(e, this);
            }
        }
        this.setNextName(wrappedName);
    }

    /**
     * Method called after a structured collection output has completed
     */
    public void finishWrappedValue(QName wrapperName, QName wrappedName) throws JacksonException
    {
        // First: wrapper to close?
        if (wrapperName != null) {
            try {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeEndElement(_xmlWriter, _streamWriteContext.getEntryCount());
                } else {
                    _xmlWriter.writeEndElement();
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwAsWriteException(e, this);
            }
        }
    }

    /**
     * Trivial helper method called when to add a replicated wrapper name
     */
    public JsonGenerator writeRepeatedPropertyName() throws JacksonException
    {
        if (!_streamWriteContext.writeName(_nextName.getLocalPart())) {
            _reportError("Can not write a property name, expecting a value");
        }
        return this;
    }

    /*
    /**********************************************************************
    /* JsonGenerator method overrides
    /**********************************************************************
     */

    @Override
    public JsonGenerator writeName(SerializableString name) throws JacksonException {
        return writeName(name.getValue());
    }

    @Override
    public JsonGenerator writeName(String name) throws JacksonException
    {
        if (!_streamWriteContext.writeName(name)) {
            _reportError("Can not write a property name, expecting a value");
        }

        String ns;
        // 30-Jan-2024, tatu: Surprise!
        if (XmlWriteFeature.AUTO_DETECT_XSI_TYPE.enabledIn(_formatFeatures)
                && "xsi:type".equals(name)) {
            setNextName(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    "type", "xsi"));
            setNextIsAttribute(true);
        } else {
            // Should this ever get called?
            ns = (_nextName == null) ? "" : _nextName.getNamespaceURI();
            _nameToEncode.namespace = ns;
            _nameToEncode.localPart = name;
            _nameProcessor.encodeName(_nameToEncode);
            setNextName(new QName(_nameToEncode.namespace, _nameToEncode.localPart));
        }
        return this;
    }

    @Override
    public JsonGenerator writePropertyId(long id) throws JacksonException {
        // 15-Aug-2019, tatu: could and probably should be improved to support
        //    buffering but...
        final String name = Long.toString(id);
        return writeName(name);
    }

    // 03-Aug-2017, tatu: We could use this as mentioned in comment below BUT
    //    since there is no counterpart for deserialization this will not
    //    help us. Approaches that could/would help probably require different
    //    handling...
    //
    //    See [dataformat-xml#4] for more context.

    // 30-Jan-2024, tatu: With 2.17 we may want to revisit this.
    /*
    @Override
    public WritableTypeId writeTypePrefix(WritableTypeId typeIdDef) throws JacksonException
    {
        // 03-Aug-2017, tatu: Due to XML oddities, we do need to massage things
        //     a bit: specifically, change WRAPPER_ARRAY into WRAPPER_OBJECT, always
        if (typeIdDef.include == WritableTypeId.Inclusion.WRAPPER_ARRAY) {
            typeIdDef.include = WritableTypeId.Inclusion.WRAPPER_OBJECT;
        }
        return super.writeTypePrefix(typeIdDef);
    }
    */

    /*
    /**********************************************************************
    /* JsonGenerator output method implementations, structural
    /**********************************************************************
     */

    @Override
    public JsonGenerator writeStartArray() throws JacksonException
    {
        _verifyValueWrite("start an array");
        _streamWriteContext = _streamWriteContext.createChildArrayContext(null);
        streamWriteConstraints().validateNestingDepth(_streamWriteContext.getNestingDepth());
        if (_xmlPrettyPrinter != null) {
            _xmlPrettyPrinter.writeStartArray(this);
        } else {
            // nothing to do here; no-operation
        }
        return this;
    }
    
    @Override
    public JsonGenerator writeStartArray(Object currValue) throws JacksonException
    {
        _verifyValueWrite("start an array");
        _streamWriteContext = _streamWriteContext.createChildArrayContext(currValue);
        streamWriteConstraints().validateNestingDepth(_streamWriteContext.getNestingDepth());
        if (_xmlPrettyPrinter != null) {
            _xmlPrettyPrinter.writeStartArray(this);
        } else {
            // nothing to do here; no-operation
        }
        return this;
    }

    @Override
    public JsonGenerator writeEndArray() throws JacksonException
    {
        if (!_streamWriteContext.inArray()) {
            _reportError("Current context not Array but "+_streamWriteContext.typeDesc());
        }
        if (_xmlPrettyPrinter != null) {
            _xmlPrettyPrinter.writeEndArray(this, _streamWriteContext.getEntryCount());
        } else {
            // nothing to do here; no-operation
        }
        _streamWriteContext = _streamWriteContext.getParent();
        return this;
    }

    @Override
    public JsonGenerator writeStartObject() throws JacksonException
    {
        _verifyValueWrite("start an object");
        _streamWriteContext = _streamWriteContext.createChildObjectContext(null);
        streamWriteConstraints().validateNestingDepth(_streamWriteContext.getNestingDepth());
        if (_xmlPrettyPrinter != null) {
            _xmlPrettyPrinter.writeStartObject(this);
        } else {
            _handleStartObject();
        }
        return this;
    }

    @Override
    public JsonGenerator writeStartObject(Object currValue) throws JacksonException
    {
        _verifyValueWrite("start an object");
        _streamWriteContext = _streamWriteContext.createChildObjectContext(currValue);
        streamWriteConstraints().validateNestingDepth(_streamWriteContext.getNestingDepth());
        if (_xmlPrettyPrinter != null) {
            _xmlPrettyPrinter.writeStartObject(this);
        } else {
            _handleStartObject();
        }
        return this;
    }

    @Override
    public JsonGenerator writeEndObject() throws JacksonException
    {
        if (!_streamWriteContext.inObject()) {
            _reportError("Current context not Object but "+_streamWriteContext.typeDesc());
        }
        _streamWriteContext = _streamWriteContext.getParent();
        if (_xmlPrettyPrinter != null) {
            // as per [Issue#45], need to suppress indentation if only attributes written:
            int count = _nextIsAttribute ? 0 : _streamWriteContext.getEntryCount();
            _xmlPrettyPrinter.writeEndObject(this, count);
        } else {
            _handleEndObject();
        }
        return this;
    }

    // note: public just because pretty printer needs to make a callback
    public final void _handleStartObject() throws JacksonException
    {
        if (_nextName == null) {
            handleMissingName();
        }
        // Need to keep track of names to make Lists work correctly
        _elementNameStack.addLast(_nextName);
        try {
            _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
    }
    
    // note: public just because pretty printer needs to make a callback
    public final void _handleEndObject() throws JacksonException
    {
        // We may want to repeat same element, so:
        if (_elementNameStack.isEmpty()) {
            throw _constructWriteException("Can not write END_ELEMENT without open START_ELEMENT", this);
        }
        _nextName = _elementNameStack.removeLast();
        try {
            // note: since attributes don't nest, can only have one attribute active, so:
            _nextIsAttribute = false;
            _xmlWriter.writeEndElement();
            // [databind-xml#172]: possibly also need indentation
            if (_elementNameStack.isEmpty() && (_xmlPrettyPrinter != null)) {
                // ... but only if it is likely to succeed:
                if (!_stax2Emulation) {
                    _xmlPrettyPrinter.writePrologLinefeed(_xmlWriter);
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
    }

    /*
    /**********************************************************************
    /* Output method implementations, textual
    /**********************************************************************
     */

    @Override
    public JsonGenerator writeString(String text) throws JacksonException
    {
        if (text == null) { // [dataformat-xml#413]
            writeNull();
            return this;
        }
        _verifyValueWrite("write String value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) { // must write attribute name and value with one call
                _xmlWriter.writeAttribute(_nextName.getNamespaceURI(), _nextName.getLocalPart(), text);
            } else if (checkNextIsUnwrapped()) {
                // [dataformat-xml#56] Should figure out how to prevent indentation for end element
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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }    
    
    @Override
    public JsonGenerator writeString(char[] text, int offset, int len) throws JacksonException
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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeString(SerializableString text) throws JacksonException {
        return writeString(text.getValue());
    }
    
    @Override
    public JsonGenerator writeRawUTF8String(byte[] text, int offset, int length) throws JacksonException
    {
        // could add support for this case if we really want it (and can make Stax2 support it)
        return _reportUnsupportedOperation();
    }

    @Override
    public JsonGenerator writeUTF8String(byte[] text, int offset, int length) throws JacksonException
    {
        // could add support for this case if we really want it (and can make Stax2 support it)
        return _reportUnsupportedOperation();
    }

    /*
    /**********************************************************************
    /* Output method implementations, unprocessed ("raw")
    /**********************************************************************
     */

    @Override
    public JsonGenerator writeRawValue(String text) throws JacksonException {
        // [dataformat-xml#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRawValue");
        }
        try {
            _verifyValueWrite("write raw value");
            if (_nextName == null) {
                handleMissingName();
            }

            if (_nextIsAttribute) {
                _xmlWriter.writeAttribute(_nextName.getNamespaceURI(), _nextName.getLocalPart(), text);
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeRaw(text);
            } else {
                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                _xmlWriter.writeRaw(text);
                _xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeRawValue(String text, int offset, int len) throws JacksonException {
        // [dataformat-xml#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRawValue");
        }
        try {
            _verifyValueWrite("write raw value");
            if (_nextName == null) {
                handleMissingName();
            }

            if (_nextIsAttribute) {
                _xmlWriter.writeAttribute(_nextName.getNamespaceURI(), _nextName.getLocalPart(), text.substring(offset, offset + len));
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeRaw(text, offset, len);
            } else {
                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                _xmlWriter.writeRaw(text, offset, len);
                _xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeRawValue(char[] text, int offset, int len) throws JacksonException {
        // [dataformat-xml#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRawValue");
        }
        _verifyValueWrite("write raw value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                _xmlWriter.writeAttribute(_nextName.getNamespaceURI(), _nextName.getLocalPart(), new String(text, offset, len));
            } else if (checkNextIsUnwrapped()) {
                _xmlWriter.writeRaw(text, offset, len);
            } else {
                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                _xmlWriter.writeRaw(text, offset, len);
                _xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeRawValue(SerializableString text) throws JacksonException {
        return _reportUnsupportedOperation();
    }

    @Override
    public JsonGenerator writeRaw(String text) throws JacksonException
    {
        // [dataformat-xml#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRaw");
        }
        try {
            _xmlWriter.writeRaw(text);
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeRaw(String text, int offset, int len) throws JacksonException
    {
        // [dataformat-xml#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRaw");
        }
        try {
            _xmlWriter.writeRaw(text, offset, len);
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeRaw(char[] text, int offset, int len) throws JacksonException
    {
        // [dataformat-xml#39]
        if (_stax2Emulation) {
            _reportUnimplementedStax2("writeRaw");
        }
        try {
            _xmlWriter.writeRaw(text, offset, len);
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeRaw(char c) throws JacksonException
    {
        return writeRaw(String.valueOf(c));
    }
    
    /*
    /**********************************************************************
    /* Output method implementations, base64-encoded binary
    /**********************************************************************
     */

    @Override
    public JsonGenerator writeBinary(Base64Variant b64variant,
    		byte[] data, int offset, int len) throws JacksonException
    {
        if (data == null) {
            writeNull();
            return this;
        }
        _verifyValueWrite("write Binary value");
        if (_nextName == null) {
            handleMissingName();
        }
        final org.codehaus.stax2.typed.Base64Variant stax2base64v = StaxUtil.toStax2Base64Variant(b64variant);
        try {
            if (_nextIsAttribute) {
                // Stax2 API only has 'full buffer' write method:
                byte[] fullBuffer = toFullBuffer(data, offset, len);
                _xmlWriter.writeBinaryAttribute(stax2base64v,
                        "", _nextName.getNamespaceURI(), _nextName.getLocalPart(), fullBuffer);
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                _xmlWriter.writeBinary(stax2base64v, data, offset, len);
            } else {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                            _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                            stax2base64v, data, offset, len);
                } else {
                    _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                    _xmlWriter.writeBinary(stax2base64v, data, offset, len);
                    _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength)
        throws JacksonException
    {
        if (data == null) {
            writeNull();
            return 0;
        }
        _verifyValueWrite("write Binary value");
        if (_nextName == null) {
            handleMissingName();
        }
        final org.codehaus.stax2.typed.Base64Variant stax2base64v = StaxUtil.toStax2Base64Variant(b64variant);
        try {
            if (_nextIsAttribute) {
                // Stax2 API only has 'full buffer' write method:
                byte[] fullBuffer = toFullBuffer(data, dataLength);
                _xmlWriter.writeBinaryAttribute(stax2base64v,
                        "", _nextName.getNamespaceURI(), _nextName.getLocalPart(), fullBuffer);
            } else if (checkNextIsUnwrapped()) {
              // should we consider pretty-printing or not?
                writeStreamAsBinary(stax2base64v, data, dataLength);

            } else {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                            _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                            stax2base64v, toFullBuffer(data, dataLength), 0, dataLength);
                } else {
                    _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                    writeStreamAsBinary(stax2base64v, data, dataLength);
                    _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        } catch (IOException e) {
            throw _wrapIOFailure(e);
        }

        return dataLength;
    }

    private void writeStreamAsBinary(org.codehaus.stax2.typed.Base64Variant stax2base64v,
            InputStream data, int len)
        throws IOException, XMLStreamException 
    {
        // base64 encodes up to 3 bytes into a 4 bytes string
        byte[] tmp = new byte[3];
        int offset = 0;
        int read;
        while ((read = data.read(tmp, offset, Math.min(3 - offset, len))) != -1) {
            offset += read;
            len -= read;
            if(offset == 3) {
                offset = 0;
                _xmlWriter.writeBinary(stax2base64v, tmp, 0, 3);
            }
            if (len == 0) {
                break;
            }
        }

        // we still have < 3 bytes in the buffer
        if (offset > 0) {
            _xmlWriter.writeBinary(stax2base64v, tmp, 0, offset);
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

    private byte[] toFullBuffer(InputStream data, final int len) throws JacksonException 
    {
        byte[] result = new byte[len];
        int offset = 0;

        for (; offset < len; ) {
            int count;
            try {
                count = data.read(result, offset, len - offset);
            } catch (IOException e) {
                throw _wrapIOFailure(e);
            }
            if (count < 0) {
                throw _constructWriteException("Too few bytes available: missing "+(len - offset)+" bytes (out of "+len+")");
            }
            offset += count;
        }
        return result;
    }

    /*
    /**********************************************************************
    /* Output method implementations, primitive
    /**********************************************************************
     */

    @Override
    public JsonGenerator writeBoolean(boolean value) throws JacksonException
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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNull() throws JacksonException
    {
        _verifyValueWrite("write null value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                // With attributes, best just leave it out, right? (since there's no way
                // to use 'xsi:nil')
            } else if (checkNextIsUnwrapped()) {
            	// as with above, best left unwritten?
            } else {
                final boolean asXsiNil = isEnabled(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL);
                if (_xmlPrettyPrinter != null) {
                    // 12-Nov-2020, tatu: Not clean, due to backwards-compat challenges..
                    //    but has to do
                    if (asXsiNil) {
                        _xmlPrettyPrinter.writeLeafXsiNilElement(_xmlWriter,
                                _nextName.getNamespaceURI(), _nextName.getLocalPart());
                    } else {
                        _xmlPrettyPrinter.writeLeafNullElement(_xmlWriter,
                                _nextName.getNamespaceURI(), _nextName.getLocalPart());
                    }
                } else {
                    if (asXsiNil) {
                        _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                        _xmlWriter.writeAttribute("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", "true");
                        _xmlWriter.writeEndElement();
                    } else {
                        _xmlWriter.writeEmptyElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                    }
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(short v) throws JacksonException {
        return writeNumber((int) v);
    }

    @Override
    public JsonGenerator writeNumber(int i) throws JacksonException
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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(long l) throws JacksonException
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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(double d) throws JacksonException
    {
        if (Double.isInfinite(d) && isEnabled(XmlWriteFeature.WRITE_XML_SCHEMA_CONFORMING_FLOATS)) {
            return writeNumber(d > 0d ? "INF" : "-INF");
        }

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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(float f) throws JacksonException
    {
        if (Float.isInfinite(f) && isEnabled(XmlWriteFeature.WRITE_XML_SCHEMA_CONFORMING_FLOATS)) {
            return writeNumber(f > 0f ? "INF" : "-INF");
        }

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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(BigDecimal dec) throws JacksonException
    {
        if (dec == null) {
            writeNull();
            return this;
        }
        _verifyValueWrite("write number");
        if (_nextName == null) {
            handleMissingName();
        }
        boolean usePlain = isEnabled(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN);
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
                    if (usePlain) {
                        _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                                _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                                dec.toPlainString(), false);
                    } else {
                        _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                                _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                                dec);
                    }
                } else {
	                _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
	                if (usePlain) {
                         _xmlWriter.writeCharacters(dec.toPlainString());
	                } else {
                         _xmlWriter.writeDecimal(dec);
	                }
	                _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(BigInteger value) throws JacksonException
    {
        if (value == null) {
            writeNull();
            return this;
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
            StaxUtil.throwAsWriteException(e, this);
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(String encodedValue) throws JacksonException, UnsupportedOperationException
    {
        return writeString(encodedValue);
    }

    /*
    /**********************************************************************
    /* Implementations, overrides for other methods
    /**********************************************************************
     */

    @Override
    protected final void _verifyValueWrite(String typeMsg) throws JacksonException
    {
        if (!_streamWriteContext.writeValue()) {
            _reportError("Cannot "+typeMsg+", expecting a property name");
        }
    }

    /*
    /**********************************************************************
    /* Low-level output handling
    /**********************************************************************
     */

    @Override
    public void flush() throws JacksonException
    {
        if (isEnabled(StreamWriteFeature.FLUSH_PASSED_TO_STREAM)) {
            try {
                _xmlWriter.flush();
            } catch (XMLStreamException e) {
                StaxUtil.throwAsWriteException(e, this);
            }
        }
    }

    @Override
    protected void _closeInput() throws JacksonException
    {
        // First: let's see that we still have buffers...
        if (isEnabled(StreamWriteFeature.AUTO_CLOSE_CONTENT)) {
            try {
                while (true) {
                    TokenStreamContext ctxt = streamWriteContext();
                    if (ctxt.inArray()) {
                        writeEndArray();
                    } else if (ctxt.inObject()) {
                        writeEndObject();
                    } else {
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // 29-Nov-2010, tatu: Stupid, stupid SJSXP doesn't do array checks, so we get
                //   hit by this as a collateral problem in some cases. Yuck.
                throw new StreamWriteException(this, e);
            }
        }
        try {
            if (_ioContext.isResourceManaged() || isEnabled(StreamWriteFeature.AUTO_CLOSE_TARGET)) {
                _xmlWriter.closeCompletely();
            } else {
                _xmlWriter.close();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsWriteException(e, this);
        }
    }

    @Override
    protected void _releaseBuffers() {
        // Nothing to do here, as we have no buffers
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
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
     * Method called in case access to native Stax2 API implementation is required.
     */
    protected void  _reportUnimplementedStax2(String missingMethod) throws JacksonException
    {
        throw _constructWriteException("Underlying Stax XMLStreamWriter (of type "
                +_originalXmlWriter.getClass().getName()
                +") does not implement Stax2 API natively and is missing method '"
                +missingMethod+"': this breaks functionality such as indentation that relies on it. "
                +"You need to upgrade to using compliant Stax implementation like Woodstox or Aalto");
    }
}
