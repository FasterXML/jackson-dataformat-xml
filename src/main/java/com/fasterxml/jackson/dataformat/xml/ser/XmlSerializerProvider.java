package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;


/**
 * We need to override some parts of {@link org.codehaus.jackson.map.SerializerProvider}
 * implementation to handle oddities of XML output, like "extra" root element.
 */
public class XmlSerializerProvider extends DefaultSerializerProvider
{
    /**
     * If all we get to serialize is a null, there's no way to figure out
     * expected root name; so let's just default to something like "<null>"...
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
        ToXmlGenerator xgen = (ToXmlGenerator) jgen;
        xgen.setNextName(rootName);
        xgen.initGenerator();
        super.serializeValue(jgen, value);
    }

    @Override
    public void serializeValue(JsonGenerator jgen, Object value, JavaType rootType)
        throws IOException, JsonProcessingException
    {
        QName rootName = _rootNameLookup.findRootName(rootType, _config);
        ToXmlGenerator xgen = (ToXmlGenerator) jgen;
        xgen.setNextName(rootName);
        xgen.initGenerator();
        super.serializeValue(jgen, value, rootType);
    }
}
