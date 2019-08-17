package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TokenStreamFactory;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.GeneratorSettings;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * We need to override some parts of
 * {@link com.fasterxml.jackson.databind.SerializerProvider}
 * implementation to handle oddities of XML output, like "extra" root element.
 */
public class XmlSerializerProvider extends DefaultSerializerProvider
{
    /**
     * If all we get to serialize is a null, there's no way to figure out
     * expected root name; so let's just default to something like "&lt;null>"...
     */
    protected final static QName ROOT_NAME_FOR_NULL = new QName("null");

    protected final XmlRootNameLookup _rootNameLookup;

    public XmlSerializerProvider(TokenStreamFactory streamFactory,
            SerializationConfig config, GeneratorSettings genSettings,
            SerializerFactory f, SerializerCache cache,
            XmlRootNameLookup rootLookup)
    {
        super(streamFactory, config, genSettings, f, cache);
        _rootNameLookup  = rootLookup;
    }

    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value) throws IOException
    {
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
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = _rootNameLookup.findRootName(cls, _config);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(cls);
            if (asArray) {
                String indexedRootName = _rootNameLookup.findWrapperForIndexedType(getTypeOfCollection(value), _config);
                _startRootArray(xgen, indexedRootName);
            }
        }
        
        // From super-class implementation
        final JsonSerializer<Object> ser = findTypedValueSerializer(cls, true);
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

    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType,
            JsonSerializer<Object> ser) throws IOException
    {
        if (value == null) {
            _serializeXmlNull(gen);
            return;
        }
        final boolean asArray;
        final ToXmlGenerator xgen = _asXmlGenerator(gen);
        if (xgen == null) { // called by convertValue()
            asArray = false;
        } else {
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = _rootNameLookup.findRootName(rootType, _config);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(rootType);
            if (asArray) {
                String indexedRootName = _rootNameLookup.findWrapperForIndexedType(getTypeOfCollection(rootType), _config);
                _startRootArray(xgen, indexedRootName);
            }
        }
        if (ser == null) {
            ser = findTypedValueSerializer(rootType, true);
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

    protected Class<?> getTypeOfCollection(Object value){
        Class<?> eleClass = value.getClass();
        if(Collection.class.isAssignableFrom(eleClass)) {
            Collection<?> collection = (Collection<?>) value;
            Iterator<?> iterator = collection.iterator();
            if (iterator.hasNext())
                eleClass = iterator.next().getClass();
        }
        return eleClass;
    }

    protected void _serializeXmlNull(JsonGenerator jgen) throws IOException
    {
        // 14-Nov-2016, tatu: As per [dataformat-xml#213], we may have explicitly
        //    configured root name...
        QName rootName = _rootNameFromConfig();
        if (rootName == null) {
            rootName = ROOT_NAME_FOR_NULL;
        }
        if (jgen instanceof ToXmlGenerator) {
            _initWithRootName((ToXmlGenerator) jgen, rootName);
        }
        super.serializeValue(jgen, null);
    }
    
    protected void _startRootArray(ToXmlGenerator xgen, String rootName) throws IOException
    {
        xgen.writeStartObject();
        // Could repeat root name, but what's the point? How to customize?
        xgen.writeFieldName(rootName);
    }    

    protected void _initWithRootName(ToXmlGenerator xgen, QName rootName) throws IOException
    {
        /* 28-Nov-2012, tatu: We should only initialize the root
         *  name if no name has been set, as per [dataformat-xml#42],
         *  to allow for custom serializers to work.
         */
        if (!xgen.setNextNameIfMissing(rootName)) {
            // however, if we are root, we... insist
            if (xgen.inRoot()) {
                xgen.setNextName(rootName);
            }
        }
        xgen.initGenerator();
        String ns = rootName.getNamespaceURI();
        /* [dataformat-xml#26] If we just try writing root element with namespace,
         * we will get an explicit prefix. But we'd rather use the default
         * namespace, so let's try to force that.
         */
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

    protected ToXmlGenerator _asXmlGenerator(JsonGenerator gen)
        throws JsonMappingException
    {
        // When converting, we actually get TokenBuffer, which is fine
        if (!(gen instanceof ToXmlGenerator)) {
            // but verify
            if (!(gen instanceof TokenBuffer)) {
                throw JsonMappingException.from(gen,
                        "XmlMapper does not with generators of type other than ToXmlGenerator; got: "+gen.getClass().getName());
            }
            return null;
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
