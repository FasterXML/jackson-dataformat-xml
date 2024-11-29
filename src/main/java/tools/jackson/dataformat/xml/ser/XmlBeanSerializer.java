package tools.jackson.dataformat.xml.ser;

import java.util.Set;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;

import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.BeanSerializer;
import tools.jackson.databind.ser.bean.BeanAsArraySerializer;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.ser.impl.ObjectIdWriter;
import tools.jackson.databind.util.NameTransformer;

/**
 * Variant of {@link BeanSerializer} for XML handling.
 * 
 * @author Pascal Gélinas
 */
public class XmlBeanSerializer extends XmlBeanSerializerBase
{
    /*
    /**********************************************************************
    /* Life-cycle: constructors
    /**********************************************************************
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

    public XmlBeanSerializer(XmlBeanSerializerBase src, Set<String> toIgnore, Set<String> toInclude) {
        super(src, toIgnore, toInclude);
    }

    protected XmlBeanSerializer(XmlBeanSerializerBase src,
            BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties)
    {
        super(src, properties, filteredProperties);
    }

    /*
    /**********************************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************************
     */

    @Override
    public ValueSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
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
    protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
        return new XmlBeanSerializer(this, toIgnore, toInclude);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties,
            BeanPropertyWriter[] filteredProperties) {
        return new XmlBeanSerializer(this, properties, filteredProperties);
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
    /**********************************************************************
    /* JsonSerializer implementation that differs between impls
    /**********************************************************************
     */

    /**
     * Main serialization method that will delegate actual output to configured
     * {@link BeanPropertyWriter} instances.
     */
    @Override
    public void serialize(Object bean, JsonGenerator g, SerializationContext ctxt)
        throws JacksonException
    {
        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, g, ctxt, true);
            return;
        }
        g.writeStartObject();
        if (_propertyFilterId != null) {
            _serializePropertiesFiltered(bean, g, ctxt, _propertyFilterId);
        } else {
            _serializeProperties(bean, g, ctxt);
        }
        g.writeEndObject();
    }

    /*
    /**********************************************************************
    /* Standard method overrides
    /**********************************************************************
     */

    @Override
    public String toString() {
        return "XmlBeanSerializer for " + handledType().getName();
    }
}
