package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * We need to override some parts of
 * {@link com.fasterxml.jackson.databind.SerializerProvider}
 * implementation to handle oddities of XML output, like "extra" root element.
 */
public class XmlSerializerProvider extends DefaultSerializerProvider
{
    // As of 2.7
    private static final long serialVersionUID = 1L;

    protected final XmlRootNameLookup _rootNameLookup;

    public XmlSerializerProvider(XmlRootNameLookup rootNames)
    {
        super();
        _rootNameLookup = rootNames;
    }

    public XmlSerializerProvider(XmlSerializerProvider src,
            SerializationConfig config, SerializerFactory f)
    {
        super(src, config, f);
        _rootNameLookup  = src._rootNameLookup;
    }

    /**
     * @since 2.8.9
     */
    protected XmlSerializerProvider(XmlSerializerProvider src) {
        super(src);
        // 21-May-2018, tatu: As per [dataformat-xml#282], should NOT really copy
        //    root name lookup as that may link back to diff version, configuration
        _rootNameLookup = new XmlRootNameLookup();
    }

    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    @Override
    public DefaultSerializerProvider copy() {
        return new XmlSerializerProvider(this);
    }

    @Override
    public DefaultSerializerProvider createInstance(SerializationConfig config,
            SerializerFactory jsf) {
        return new XmlSerializerProvider(this, config, jsf);
    }

    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value) throws IOException
    {
        _generator = gen;
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        final Class<?> cls = value.getClass();
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            // [dataformat-xml#441]: allow ObjectNode unwrapping
            if (_shouldUnwrapObjectNode(xgen, value)) {
                _serializeUnwrappedObjectNode(xgen, value, null);
                return;
            }
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = _rootNameLookup.findRootName(cls, _config);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(cls);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        
        // From super-class implementation
        final JsonSerializer<Object> ser = findTypedValueSerializer(cls, true, null);
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) { // but wrap RuntimeExceptions, to get path information
            throw _wrapAsIOE(gen, e);
        }
        // end of super-class implementation

        if (asArray) {
            gen.writeEndObject();
        }
    }

    @Override // since 2.11.1, was missing before
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType) throws IOException
    {
        serializeValue(gen, value, rootType, null);
    }

    // @since 2.1
    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType,
            JsonSerializer<Object> ser) throws IOException
    {
        _generator = gen;
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        // Let's ensure types are compatible at this point
        if ((rootType != null) && !rootType.getRawClass().isAssignableFrom(value.getClass())) {
            _reportIncompatibleRootType(value, rootType);
        }
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            // [dataformat-xml#441]: allow ObjectNode unwrapping
            if (_shouldUnwrapObjectNode(xgen, value)) {
                _serializeUnwrappedObjectNode(xgen, value, ser);
                return;
            }
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = (rootType == null)
                        ? _rootNameLookup.findRootName(value.getClass(), _config)
                        : _rootNameLookup.findRootName(rootType, _config);
            }
            _initWithRootName(xgen, rootName);
            asArray = (rootType == null)
                    ? TypeUtil.isIndexedType(value.getClass())
                        : TypeUtil.isIndexedType(rootType);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        if (ser == null) {
            ser = findTypedValueSerializer(rootType, true, null);
        }
        // From super-class implementation
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsIOE(gen, e);
        }
        // end of super-class implementation
        if (asArray) {
            gen.writeEndObject();
        }
    }

    @SuppressWarnings("resource")
    @Override // since 2.11.1, was missing before
    public void serializePolymorphic(JsonGenerator gen, Object value, JavaType rootType,
            JsonSerializer<Object> valueSer, TypeSerializer typeSer)
        throws IOException
    {
        _generator = gen;
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        // Let's ensure types are compatible at this point
        if ((rootType != null) && !rootType.getRawClass().isAssignableFrom(value.getClass())) {
            _reportIncompatibleRootType(value, rootType);
        }
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = (rootType == null)
                        ? _rootNameLookup.findRootName(value.getClass(), _config)
                        : _rootNameLookup.findRootName(rootType, _config);
            }
            _initWithRootName(xgen, rootName);
            asArray = (rootType == null)
                    ? TypeUtil.isIndexedType(value.getClass())
                    : TypeUtil.isIndexedType(rootType);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        // 21-May-2020: See comments in `jackson-databind/DefaultSerializerProvider`
        if (valueSer == null) {
            if ((rootType != null) && rootType.isContainerType()) {
                valueSer = findValueSerializer(rootType, null);
            } else {
                valueSer = findValueSerializer(value.getClass(), null);
            }
        }
        // From super-class implementation
        try {
            valueSer.serializeWithType(value, gen, this, typeSer);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsIOE(gen, e);
        }
        // end of super-class implementation
        if (asArray) {
            gen.writeEndObject();
        }
    }

    protected void _serializeXmlNull(JsonGenerator gen) throws IOException
    {
        // 14-Nov-2016, tatu: As per [dataformat-xml#213], we may have explicitly
        //    configured root name...
        QName rootName = _rootNameFromConfig();
        if (rootName == null) {
            rootName = XmlRootNameLookup.ROOT_NAME_FOR_NULL;
        }
        if (gen instanceof ToXmlGenerator) {
            _initWithRootName((ToXmlGenerator) gen, rootName);
        }
        super.serializeValue(gen, null);
    }

    protected void _startRootArray(ToXmlGenerator xgen, QName rootName) throws IOException
    {
        xgen.writeStartObject();
        // Could repeat root name, but what's the point? How to customize?
        xgen.writeFieldName("item");
    }    

    protected void _initWithRootName(ToXmlGenerator xgen, QName rootName) throws IOException
    {
        // 28-Nov-2012, tatu: We should only initialize the root name if no name has been
        //   set, as per [dataformat-xml#42], to allow for custom serializers to work.
        if (!xgen.setNextNameIfMissing(rootName)) {
            // however, if we are root, we... insist
            if (xgen.inRoot()) {
                xgen.setNextName(rootName);
            }
        }
        xgen.initGenerator();
        String ns = rootName.getNamespaceURI();
        // [dataformat-xml#26] If we just try writing root element with namespace,
        // we will get an explicit prefix. But we'd rather use the default
        // namespace, so let's try to force that.
        if (ns != null && ns.length() > 0) {
            try {
                xgen.getStaxWriter().setDefaultNamespace(ns);
            } catch (XMLStreamException e) {
                StaxUtil.throwAsGenerationException(e, xgen);
            }
        }
    }

    protected QName _rootNameFromConfig()
    {
        PropertyName name = _config.getFullRootName();
        if (name == null) {
            return null;
        }
        String ns = name.getNamespace();
        if (ns == null || ns.isEmpty()) {
            return new QName(name.getSimpleName());
        }
        return new QName(ns, name.getSimpleName());
    }

    // @since 2.13
    protected boolean _shouldUnwrapObjectNode(ToXmlGenerator xgen, Object value)
    {
        return xgen.isEnabled(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE)
                && (value instanceof ObjectNode)
                && (((ObjectNode) value).size() == 1);
    }

    // @since 2.13
    protected void _serializeUnwrappedObjectNode(ToXmlGenerator xgen, Object value,
            JsonSerializer<Object> ser) throws IOException
    {
        ObjectNode root = (ObjectNode) value;
        Map.Entry<String, JsonNode> entry = root.fields().next();
        final JsonNode newRoot = entry.getValue();

        // No namespace associated with JsonNode:
        _initWithRootName(xgen, new QName(entry.getKey()));
        if (ser == null) {
            ser = findTypedValueSerializer(newRoot.getClass(), true, null);
        }
        // From super-class implementation
        try {
            ser.serialize(newRoot, xgen, this);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsIOE(xgen, e);
        }
    }

    protected ToXmlGenerator _asXmlGenerator(JsonGenerator gen)
        throws JsonMappingException
    {
        if (!(gen instanceof ToXmlGenerator)) {
            // [dataformat-xml#71]: We sometimes get TokenBuffer, which is fine
            if (gen instanceof TokenBuffer) {
                return null;
            }
            // but verify
            throw JsonMappingException.from(gen,
                    "XmlMapper does not work with generators of type other than `ToXmlGenerator`; got: `"
                            +gen.getClass().getName()+"`");
        }
        return (ToXmlGenerator) gen;
    }    

    protected IOException _wrapAsIOE(JsonGenerator g, Exception e) {
        if (e instanceof IOException) {
            return (IOException) e;
        }
        String msg = e.getMessage();
        if (msg == null) {
            msg = "[no message for "+e.getClass().getName()+"]";
        }
        return new JsonMappingException(g, msg, e);
    }
}
