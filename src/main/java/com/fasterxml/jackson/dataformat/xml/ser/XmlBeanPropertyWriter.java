package com.fasterxml.jackson.dataformat.xml.ser;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

/**
 * Property writer sub-class used for handling element wrapping needed for serializing
 * collection (array, Collection; possibly Map) types.
 */
public class XmlBeanPropertyWriter
    extends BeanPropertyWriter
{
    private static final long serialVersionUID = 1L;

    /*
    /**********************************************************
    /* Config settings
    /**********************************************************
     */

    /**
     * Element name used as wrapper for collection.
     */
    protected final QName _wrapperQName;

    /**
     * Element name used for items in the collection
     */
    protected final QName _wrappedQName;

    /*
    /**********************************************************
    /* Life-cycle: construction, configuration
    /**********************************************************
     */

    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped,
            PropertyName wrapperName, PropertyName wrappedName) {
        this(wrapped, wrapperName, wrappedName, null);
    }

    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped,
            PropertyName wrapperName, PropertyName wrappedName,
            JsonSerializer<Object> serializer)
    {
        super(wrapped);
        _wrapperQName = _qname(wrapperName);
        _wrappedQName = _qname(wrappedName);

        if (serializer != null) {
            assignSerializer(serializer);
        }
    }

    private QName _qname(PropertyName n)
    {
        String ns = n.getNamespace();
        if (ns == null) {
            ns = "";
        }
        return new QName(ns, n.getSimpleName());
    }
    
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    /**
     * Overridden version so that we can wrap output within wrapper element if
     * and as necessary.
     */
    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov)
        throws Exception
    {
        Object value = get(bean);

        /* 13-Feb-2014, tatu: As per [#103], default handling does not really
         *   work here. Rather, we need just a wrapping and should NOT call
         *   null handler, as it does not know what to do...
         *   
         *   Question, however, is what should it be serialized as. We have two main
         *   choices; equivalent empty List, and "nothing" (missing). Let's start with
         *   empty List? But producing missing entry is non-trivial...
         */
        if (value == null) {
            // if (_nullSerializer != null) { ... }

            // For Empty List, we'd do this:
            /*
            @SuppressWarnings("resource")
            final ToXmlGenerator xmlGen = (jgen instanceof ToXmlGenerator) ? (ToXmlGenerator) jgen : null;
            if (xmlGen != null) {
                xmlGen.startWrappedValue(_wrapperQName, _wrappedQName);
                xmlGen.finishWrappedValue(_wrapperQName, _wrappedQName);
            }
            */
            // but for missing thing, well, just output nothing
            
            return;
        }

        // then find serializer to use
        JsonSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap map = _dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(map, cls, prov);
            }
        }
        // and then see if we must suppress certain values (default, empty)
        if (_suppressableValue != null) {
            if (MARKER_FOR_EMPTY == _suppressableValue) {
                if (ser.isEmpty(prov, value)) {
                    return;
                }
            } else if (_suppressableValue.equals(value)) {
                return;
            }
        }
        // For non-nulls: simple check for direct cycles
        if (value == bean) {
            // NOTE: method signature here change 2.3->2.4
            if (_handleSelfReference(bean, jgen, prov, ser)) {
                return;
            }
        }

        final ToXmlGenerator xmlGen = (jgen instanceof ToXmlGenerator) ? (ToXmlGenerator) jgen : null;
        // Ok then; addition we want to do is to add wrapper element, and that's what happens here
        // 19-Aug-2013, tatu: ... except for those nasty 'convertValue()' calls...
        if (xmlGen != null) {
            xmlGen.startWrappedValue(_wrapperQName, _wrappedQName);
        }
        jgen.writeFieldName(_name);
        if (_typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, _typeSerializer);
        }
        if (xmlGen != null) {
            xmlGen.finishWrappedValue(_wrapperQName, _wrappedQName);
        }
    }
}
