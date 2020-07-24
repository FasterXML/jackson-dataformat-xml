package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Copy of {@link UnwrappingBeanSerializer} required to extend
 * {@link XmlBeanSerializerBase} for XML-specific handling.
 * 
 * @author Pascal Gélinas
 * 
 */
public class UnwrappingXmlBeanSerializer extends XmlBeanSerializerBase
{
    private static final long serialVersionUID = 1L;
 
    /**
     * Transformer used to add prefix and/or suffix for properties of unwrapped
     * POJO.
     */
    protected final NameTransformer _nameTransformer;

    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */

    /**
     * Constructor used for creating unwrapping instance of a standard
     * <code>BeanSerializer</code>
     */
    public UnwrappingXmlBeanSerializer(XmlBeanSerializerBase src, NameTransformer transformer)
    {
        super(src, transformer);
        _nameTransformer = transformer;
    }

    public UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src, ObjectIdWriter objectIdWriter)
    {
        super(src, objectIdWriter);
        _nameTransformer = src._nameTransformer;
    }

    public UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId)
    {
        super(src, objectIdWriter, filterId);
        _nameTransformer = src._nameTransformer;
    }

    protected UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src,
            Set<String> toIgnore, Set<String> toInclude)
    {
        super(src, toIgnore, toInclude);
        _nameTransformer = src._nameTransformer;
    }

    protected UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src,
            BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties)
    {
        super(src, properties, filteredProperties);
        _nameTransformer = src._nameTransformer;
    }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************
     */

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer)
    {
        // !!! 23-Jan-2012, tatu: Should we chain transformers?
        return new UnwrappingXmlBeanSerializer(this, transformer);
    }

    @Override
    public boolean isUnwrappingSerializer()
    {
        return true; // sure is
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter)
    {
        return new UnwrappingXmlBeanSerializer(this, objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId)
    {
        return new UnwrappingXmlBeanSerializer(this, _objectIdWriter, filterId);
    }

    @Override // since 2.12
    protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
        return new UnwrappingXmlBeanSerializer(this, toIgnore, toInclude);
    }

    @Override // since 2.11.1
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties,
            BeanPropertyWriter[] filteredProperties) {
        return new UnwrappingXmlBeanSerializer(this, properties, filteredProperties);
    }

    /**
     * JSON Array output can not be done if unwrapping operation is requested;
     * so implementation will simply return 'this'.
     */
    @Override
    protected BeanSerializerBase asArraySerializer() {
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
    public final void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, jgen, provider, false);
            return;
        }
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return "UnwrappingXmlBeanSerializer for " + handledType().getName();
    }
}
