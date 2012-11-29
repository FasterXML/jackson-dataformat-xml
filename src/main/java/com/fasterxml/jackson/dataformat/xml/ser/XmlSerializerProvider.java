package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
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
        QName rootName = (value == null) ? ROOT_NAME_FOR_NULL
                : _rootNameLookup.findRootName(value.getClass(), _config);
        _initWithRootName(jgen, rootName);
        super.serializeValue(jgen, value);
    }

    @Override
    public void serializeValue(JsonGenerator jgen, Object value, JavaType rootType)
        throws IOException, JsonProcessingException
    {
        QName rootName = _rootNameLookup.findRootName(rootType, _config);
        _initWithRootName(jgen, rootName);
        super.serializeValue(jgen, value, rootType);
    }

    // @since 2.1
    @Override
    public void serializeValue(JsonGenerator jgen, Object value, JavaType rootType,
            JsonSerializer<Object> ser)
        throws IOException, JsonGenerationException
    {
        QName rootName = _rootNameLookup.findRootName(rootType, _config);
        _initWithRootName(jgen, rootName);
        super.serializeValue(jgen, value, rootType, ser);
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
        /* [Issue-26] If we just try writing root element with namespace,
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
}
