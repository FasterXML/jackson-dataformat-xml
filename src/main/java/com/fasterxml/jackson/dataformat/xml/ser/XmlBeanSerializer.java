package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Variant of {@link BeanSerializer} for XML handling.
 * 
 * @author Pascal Gélinas
 */
public class XmlBeanSerializer extends XmlBeanSerializerBase
{
    private static final long serialVersionUID = 1L;

    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */
    public XmlBeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    public XmlBeanSerializer(XmlBeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
    }

    public XmlBeanSerializer(XmlBeanSerializerBase src, ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
    }

    public XmlBeanSerializer(XmlBeanSerializerBase src, String[] toIgnore) {
        super(src, toIgnore);
    }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************
     */


    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new UnwrappingXmlBeanSerializer(this, unwrapper);
    }
    
    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new XmlBeanSerializer(this, objectIdWriter, _propertyFilterId);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new XmlBeanSerializer(this, _objectIdWriter, filterId);
    }

    @Override
    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
        return new XmlBeanSerializer(this, toIgnore);
    }

    /**
     * Implementation has to check whether as-array serialization
     * is possible reliably; if (and only if) so, will construct
     * a {@link BeanAsArraySerializer}, otherwise will return this
     * serializer as is.
     */
    @Override
    protected BeanSerializerBase asArraySerializer()
    {
        /* Can not:
         * 
         * - have Object Id (may be allowed in future)
         * - have any getter
         * 
         */
        if ((_objectIdWriter == null)
                && (_anyGetterWriter == null)
                && (_propertyFilterId == null)
                ) {
            return new BeanAsArraySerializer(this);
        }
        // already is one, so:
        return this;
    }

    /*
    /**********************************************************
    /* JsonSerializer implementation that differs between impls
    /**********************************************************
     */

    /**
     * Main serialization method that will delegate actual output to configured
     * {@link BeanPropertyWriter} instances.
     */
    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException
    {
        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, jgen, provider, true);
            return;
        }
        jgen.writeStartObject();
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }
        jgen.writeEndObject();
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString() {
        return "XmlBeanSerializer for " + handledType().getName();
    }
}
