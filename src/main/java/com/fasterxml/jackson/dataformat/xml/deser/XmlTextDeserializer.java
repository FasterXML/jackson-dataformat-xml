package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Delegating deserializer that is used in the special cases where
 * we may sometimes see a "plain" String value but need to map it
 * as if it was a property of POJO. The impedance is introduced by
 * heuristic conversion from XML events into rough JSON equivalents;
 * and this is one work-around that can only be done after the fact.
 */
public class XmlTextDeserializer
    extends DelegatingDeserializer
{
    private static final long serialVersionUID = 1L;

    /**
     * Property index of the "XML text property"; needed for finding actual
     * property instance after resolution and contextualization: instance
     * may change, but index will remain constant.
     */
    protected final int _xmlTextPropertyIndex;
    
    /**
     * Actual property that is indicated to be of type "XML Text" (and
     * is the only element-valued property)
     */
    protected final SettableBeanProperty _xmlTextProperty;

    protected final ValueInstantiator _valueInstantiator;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    public XmlTextDeserializer(BeanDeserializerBase delegate, SettableBeanProperty prop)
    {
        super(delegate);
        _xmlTextProperty = prop;
        _xmlTextPropertyIndex = prop.getPropertyIndex();
        _valueInstantiator = delegate.getValueInstantiator();
    }
    
    public XmlTextDeserializer(BeanDeserializerBase delegate, int textPropIndex)
    {
        super(delegate);
        _xmlTextPropertyIndex = textPropIndex;
        _valueInstantiator = delegate.getValueInstantiator();
        _xmlTextProperty = delegate.findProperty(textPropIndex);
    }
    
    /*
    /**********************************************************************
    /* Abstract method implementations
    /**********************************************************************
     */

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee0) {
        // default not enough, as we need to create a new wrapping deserializer
        // even if delegatee does not change
        throw new IllegalStateException("Internal error: should never get called");
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property)
        throws JsonMappingException
    {
        return new XmlTextDeserializer(_verifyDeserType(_delegatee), _xmlTextPropertyIndex);
    }

    /*
    /**********************************************************************
    /* Overridden deserialization methods
    /**********************************************************************
     */

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            Object bean = _valueInstantiator.createUsingDefault(ctxt);
            _xmlTextProperty.deserializeAndSet(jp, ctxt, bean);
            return bean;
        }
        return _delegatee.deserialize(jp,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt,
            Object bean)
        throws IOException, JsonProcessingException
    {
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            _xmlTextProperty.deserializeAndSet(jp, ctxt, bean);
            return bean;
        }
        return ((JsonDeserializer<Object>)_delegatee).deserialize(jp, ctxt, bean);
    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return _delegatee.deserializeWithType(jp, ctxt, typeDeserializer);
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected BeanDeserializerBase _verifyDeserType(JsonDeserializer<?> deser)
    {
        if (!(deser instanceof BeanDeserializerBase)) {
            throw new IllegalArgumentException("Can not change delegate to be of type "
                    +deser.getClass().getName());
        }
        return (BeanDeserializerBase) deser;
    }
}
