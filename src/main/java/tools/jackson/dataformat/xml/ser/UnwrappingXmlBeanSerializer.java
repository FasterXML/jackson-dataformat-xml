package tools.jackson.dataformat.xml.ser;

import java.util.Set;

import tools.jackson.core.*;

import tools.jackson.databind.*;
import tools.jackson.databind.ser.*;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.ser.bean.UnwrappingBeanSerializer;
import tools.jackson.databind.ser.impl.ObjectIdWriter;
import tools.jackson.databind.util.NameTransformer;

/**
 * Copy of {@link UnwrappingBeanSerializer} required to extend
 * {@link XmlBeanSerializerBase} for XML-specific handling.
 */
public class UnwrappingXmlBeanSerializer extends XmlBeanSerializerBase
{
    /**
     * Transformer used to add prefix and/or suffix for properties of unwrapped
     * POJO.
     */
    protected final NameTransformer _nameTransformer;

    /*
    /**********************************************************************
    /* Life-cycle: constructors
    /**********************************************************************
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
    /**********************************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************************
     */

    @Override
    public ValueSerializer<Object> unwrappingSerializer(NameTransformer transformer)
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
    /**********************************************************************
    /* JsonSerializer implementation that differs between impls
    /**********************************************************************
     */

    /**
     * Main serialization method that will delegate actual output to configured
     * {@link BeanPropertyWriter} instances.
     */
    @Override
    public final void serialize(Object bean, JsonGenerator g, SerializationContext ctxt)
        throws JacksonException
    {
        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, g, ctxt, false);
            return;
        }
        if (_propertyFilterId != null) {
            _serializePropertiesFiltered(bean, g, ctxt, _propertyFilterId);
        } else {
            _serializeProperties(bean, g, ctxt);
        }
    }

    /*
    /**********************************************************************
    /* Standard methods
    /**********************************************************************
     */

    @Override
    public String toString()
    {
        return "UnwrappingXmlBeanSerializer for " + handledType().getName();
    }
}
