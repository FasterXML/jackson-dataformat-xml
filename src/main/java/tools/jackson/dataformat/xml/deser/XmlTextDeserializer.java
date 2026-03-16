package tools.jackson.dataformat.xml.deser;

import tools.jackson.core.*;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.*;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.util.TokenBuffer;

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
    protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee0) {
        // default not enough, as we need to create a new wrapping deserializer
        // even if delegatee does not change
        throw new IllegalStateException("Internal error: should never get called");
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property)
    {
        // 15-Nov-2017, tatu: Important -- MUST contextualize thing we delegate to
        JavaType vt = ctxt.constructType(_delegatee.handledType());
        ValueDeserializer<?> del = ctxt.handleSecondaryContextualization(_delegatee,
                property, vt);
        return new XmlTextDeserializer(_verifyDeserType(del), _xmlTextPropertyIndex);
    }

    /*
    /**********************************************************************
    /* Overridden deserialization methods
    /**********************************************************************
     */

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt)
        throws JacksonException
    {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            if (_valueInstantiator.canCreateUsingDefault()) {
                Object bean = _valueInstantiator.createUsingDefault(ctxt);
                _xmlTextProperty.deserializeAndSet(p, ctxt, bean);
                return bean;
            }
            // [dataformat-xml#615]: No default constructor (e.g. records);
            // synthesize object tokens so the delegate can use property-based creators
            return _deserializeFromStringViaDelegate(p, ctxt);
        }
        return _delegatee.deserialize(p,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean)
        throws JacksonException
    {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            _xmlTextProperty.deserializeAndSet(p, ctxt, bean);
            return bean;
        }
        return ((ValueDeserializer<Object>)_delegatee).deserialize(p, ctxt, bean);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws JacksonException
    {
        return _delegatee.deserializeWithType(p, ctxt, typeDeserializer);
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected BeanDeserializerBase _verifyDeserType(ValueDeserializer<?> deser)
    {
        if (!(deser instanceof BeanDeserializerBase)) {
            throw new IllegalArgumentException("Can not change delegate to be of type "
                    +deser.getClass().getName());
        }
        return (BeanDeserializerBase) deser;
    }

    /**
     * [dataformat-xml#615]: When the parser sees a bare VALUE_STRING but the type
     * has no default constructor (e.g. Java records), wrap the text value as
     * {@code { "": "text" }} so the delegate can use its property-based creator.
     *
     * @since 3.2
     */
    private Object _deserializeFromStringViaDelegate(JsonParser p,
            DeserializationContext ctxt)
        throws JacksonException
    {
        try (TokenBuffer tb = ctxt.bufferForInputBuffering(p)) {
            tb.writeStartObject();
            tb.writeName(_xmlTextProperty.getName());
            tb.writeString(p.getString());
            tb.writeEndObject();
            try (JsonParser syntheticParser = tb.asParserOnFirstToken(ctxt, p)) {
                return _delegatee.deserialize(syntheticParser, ctxt);
            }
        }
    }
}
