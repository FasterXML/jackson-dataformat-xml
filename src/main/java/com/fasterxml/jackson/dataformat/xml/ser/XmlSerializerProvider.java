package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.WrappedIOException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.GeneratorSettings;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializationContextExt;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * We need to override some parts of
 * {@link com.fasterxml.jackson.databind.SerializerProvider}
 * implementation to handle oddities of XML output, like "extra" root element.
 */
public class XmlSerializerProvider extends SerializationContextExt
{
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
    public void serializeValue(JsonGenerator gen, Object value) throws JacksonException
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
            QName rootName = _rootNameFromConfig();
            if (rootName == null) {
                rootName = _rootNameLookup.findRootName(this, cls);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(cls);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        
        // From super-class implementation
        final JsonSerializer<Object> ser = findTypedValueSerializer(cls, true);
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) { // but wrap RuntimeExceptions, to get path information
            throw _wrapAsJacksonE(gen, e);
        }
        // end of super-class implementation

        if (asArray) {
            gen.writeEndObject();
        }
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType) throws JacksonException
    {
        serializeValue(gen, value, rootType, null);
    }

    @SuppressWarnings("resource")
    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType,
            JsonSerializer<Object> ser) throws JacksonException
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
                rootName = _rootNameLookup.findRootName(this, rootType);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(rootType);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        if (ser == null) {
            ser = findTypedValueSerializer(rootType, true);
        }
        // From super-class implementation
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsJacksonE(gen, e);
        }
        // end of super-class implementation
        if (asArray) {
            gen.writeEndObject();
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void serializePolymorphic(JsonGenerator gen, Object value, JavaType rootType,
            JsonSerializer<Object> valueSer, TypeSerializer typeSer)
        throws JacksonException
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
                rootName = _rootNameLookup.findRootName(this, rootType);
            }
            _initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(rootType);
            if (asArray) {
                _startRootArray(xgen, rootName);
            }
        }
        // 21-May-2020: See comments in `jackson-databind/DefaultSerializerProvider`
        if (valueSer == null) {
            if ((rootType != null) && rootType.isContainerType()) {
                valueSer = handleRootContextualization(findValueSerializer(rootType));
            } else {
                valueSer = handleRootContextualization(findValueSerializer(value.getClass()));
            }
        }
        // From super-class implementation
        try {
            valueSer.serializeWithType(value, gen, this, typeSer);
        } catch (Exception e) { // but others do need to be, to get path etc
            throw _wrapAsJacksonE(gen, e);
        }
        // end of super-class implementation
        if (asArray) {
            gen.writeEndObject();
        }
    }
    
    protected void _serializeXmlNull(JsonGenerator gen) throws JacksonException
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

    protected void _startRootArray(ToXmlGenerator xgen, QName rootName) throws JacksonException
    {
        xgen.writeStartObject();
        // Could repeat root name, but what's the point? How to customize?
        xgen.writeName("item");
    }

    protected void _initWithRootName(ToXmlGenerator xgen, QName rootName) throws JacksonException
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
        /* [dataformat-xml#26] If we just try writing root element with namespace,
         * we will get an explicit prefix. But we'd rather use the default
         * namespace, so let's try to force that.
         */
        if (ns != null && ns.length() > 0) {
            try {
                xgen.getStaxWriter().setDefaultNamespace(ns);
            } catch (XMLStreamException e) {
                StaxUtil.throwAsWriteException(e, xgen);
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
    {
        if (!(gen instanceof ToXmlGenerator)) {
            // [dataformat-xml#71]: We sometimes get TokenBuffer, which is fine
            if (gen instanceof TokenBuffer) {
                return null;
            }
            // but verify
            throw DatabindException.from(gen,
                    "XmlMapper does not work with generators of type other than `ToXmlGenerator`; got: `"
                            +gen.getClass().getName()+"`");
        }
        return (ToXmlGenerator) gen;
    }    

    protected JacksonException _wrapAsJacksonE(JsonGenerator g, Exception e)
    {
        if (e instanceof IOException) {
            return WrappedIOException.construct((IOException) e);
        }
        // 17-Jan-2021, tatu: Should we do something else here? Presumably
        //    this exception has map set up
        if (e instanceof DatabindException) {
            throw (DatabindException) e;
        }
        String msg = e.getMessage();
        if (msg == null) {
            msg = "[no message for "+e.getClass().getName()+"]";
        }
        return DatabindException.from(g, msg, e);
    }
}
