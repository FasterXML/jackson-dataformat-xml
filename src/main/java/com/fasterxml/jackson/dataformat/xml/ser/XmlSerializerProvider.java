package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
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
    private static final long serialVersionUID = 8525947864862035821L;

    /**
     * If all we get to serialize is a null, there's no way to figure out
     * expected root name; so let's just default to something like "&lt;null>"...
     */
    protected final static QName ROOT_NAME_FOR_NULL = new QName("null");
    
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
    
    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    @Override
    public DefaultSerializerProvider createInstance(SerializationConfig config,
            SerializerFactory jsf)
    {
        return new XmlSerializerProvider(this, config, jsf);
    }

    @Override
    public void serializeValue(JsonGenerator jgen, Object value)
        throws IOException, JsonProcessingException
    {
        if (value == null) {
            _serializeNull(jgen);
            return;
        }
        Class<?> cls = value.getClass();
        QName rootName = _rootNameFromConfig();
        if (rootName == null) {
            rootName = _rootNameLookup.findRootName(cls, _config);
        }
        _initWithRootName(jgen, rootName);
        final boolean asArray = Collection.class.isAssignableFrom(cls) ||
                (cls.isArray() && cls != byte[].class);
        if (asArray) {
            _startRootArray(jgen, rootName);
        }
        
        // From super-class implementation
        final JsonSerializer<Object> ser = findTypedValueSerializer(cls, true, null);
        try {
            ser.serialize(value, jgen, this);
        } catch (IOException ioe) { // As per [JACKSON-99], pass IOException and subtypes as-is
            throw ioe;
        } catch (Exception e) { // but wrap RuntimeExceptions, to get path information
            String msg = e.getMessage();
            if (msg == null) {
                msg = "[no message for "+e.getClass().getName()+"]";
            }
            throw new JsonMappingException(msg, e);
        }
        // end of super-class implementation

        if (asArray) {
            jgen.writeEndObject();
        }
    }
    
    @Override
    public void serializeValue(JsonGenerator jgen, Object value, JavaType rootType)
        throws IOException, JsonProcessingException
    {
        if (value == null) {
            _serializeNull(jgen);
            return;
        }
        QName rootName = _rootNameFromConfig();
        if (rootName == null) {
            rootName = _rootNameLookup.findRootName(rootType, _config);
        }
        _initWithRootName(jgen, rootName);
        final boolean asArray = TypeUtil.isIndexedType(rootType);
        if (asArray) {
            _startRootArray(jgen, rootName);
        }

        final JsonSerializer<Object> ser = findTypedValueSerializer(rootType, true, null);
        // From super-class implementation
        try {
            ser.serialize(value, jgen, this);
        } catch (IOException ioe) { // no wrapping for IO (and derived)
            throw ioe;
        } catch (Exception e) { // but others do need to be, to get path etc
            String msg = e.getMessage();
            if (msg == null) {
                msg = "[no message for "+e.getClass().getName()+"]";
            }
            throw new JsonMappingException(msg, e);
        }
        // end of super-class implementation

        if (asArray) {
            jgen.writeEndObject();
        }
    }
    
    // @since 2.1
    @Override
    public void serializeValue(JsonGenerator jgen, Object value, JavaType rootType,
            JsonSerializer<Object> ser)
        throws IOException, JsonGenerationException
    {
        if (value == null) {
            _serializeNull(jgen);
            return;
        }
        QName rootName = _rootNameFromConfig();
        if (rootName == null) {
            rootName = _rootNameLookup.findRootName(rootType, _config);
        }
        _initWithRootName(jgen, rootName);
        final boolean asArray = TypeUtil.isIndexedType(rootType);
        if (asArray) {
            _startRootArray(jgen, rootName);
        }
        if (ser == null) {
            ser = findTypedValueSerializer(rootType, true, null);
        }
        // From super-class implementation
        try {
            ser.serialize(value, jgen, this);
        } catch (IOException ioe) { // no wrapping for IO (and derived)
            throw ioe;
        } catch (Exception e) { // but others do need to be, to get path etc
            String msg = e.getMessage();
            if (msg == null) {
                msg = "[no message for "+e.getClass().getName()+"]";
            }
            throw new JsonMappingException(msg, e);
        }
        // end of super-class implementation
        if (asArray) {
            jgen.writeEndObject();
        }
    }

    protected void _startRootArray(JsonGenerator jgen, QName rootName)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        // Could repeat root name, but what's the point? How to customize?
        ((ToXmlGenerator) jgen).writeFieldName("item");
    }    

    @Override
    protected void _serializeNull(JsonGenerator jgen)
            throws IOException, JsonProcessingException
    {
        _initWithRootName(jgen, ROOT_NAME_FOR_NULL);
        super.serializeValue(jgen, null);
    }

    protected void _initWithRootName(JsonGenerator jgen, QName rootName)
            throws IOException, JsonProcessingException
    {
        ToXmlGenerator xgen = (ToXmlGenerator) jgen;
        /* 28-Nov-2012, tatu: We should only initialize the root
         *  name if no name has been set, as per [Issue#42],
         *  to allow for custom serializers to work.
         */
        if (!xgen.setNextNameIfMissing(rootName)) {
            // however, if we are root, we... insist
            if (xgen.getOutputContext().inRoot()) {
                xgen.setNextName(rootName);
            }
        }
        xgen.initGenerator();
        String ns = rootName.getNamespaceURI();
        /* [Issue#26] If we just try writing root element with namespace,
         * we will get an explicit prefix. But we'd rather use the default
         * namespace, so let's try to force that.
         */
        if (ns != null && ns.length() > 0) {
            try {
                xgen.getStaxWriter().setDefaultNamespace(ns);
            } catch (XMLStreamException e) {
                StaxUtil.throwXmlAsIOException(e);
            }
        }
    }

    protected QName _rootNameFromConfig()
    {
        String name = _config.getRootName();
        return (name == null) ? null : new QName(name);
    }
}
