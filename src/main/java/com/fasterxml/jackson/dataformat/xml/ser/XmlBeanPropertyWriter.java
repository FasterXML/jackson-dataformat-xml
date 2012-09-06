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
    /*
    /**********************************************************
    /* Config settings
    /**********************************************************
     */

    /**
     * Element name used as wrapper for collection.
     */
    protected final QName _wrapperName;

    /**
     * Element name used for items in the collection
     */
    protected final QName _wrappedName;
    
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
        _wrapperName = _qname(wrapperName);
        _wrappedName = _qname(wrappedName);

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
        
        /* Hmmh. Does the default null serialization work ok here? For now let's assume
         * it does; can change later if not.
         */
        if (value == null) {
            if (_nullSerializer != null) {
                jgen.writeFieldName(_name);
                _nullSerializer.serialize(null, jgen, prov);
            }
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
                if (ser.isEmpty(value)) {
                    return;
                }
            } else if (_suppressableValue.equals(value)) {
                return;
            }
        }
        // For non-nulls: simple check for direct cycles
        if (value == bean) {
            _handleSelfReference(bean, ser);
        }

        // Ok then; addition we want to do is to add wrapper element, and that's what happens here
        ToXmlGenerator xmlGen = (ToXmlGenerator) jgen;
        xmlGen.startWrappedValue(_wrapperName, _wrappedName);
        jgen.writeFieldName(_name);
        if (_typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, _typeSerializer);
        }
        xmlGen.finishWrappedValue(_wrapperName, _wrappedName);
    }
}
