package tools.jackson.dataformat.xml.deser;

import java.util.*;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.util.JsonParserDelegate;
import tools.jackson.databind.util.TokenBuffer;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.*;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.dataformat.xml.util.TypeUtil;

/**
 * Delegating deserializer whose only function is to handle case of
 * "unwrapped" List/array deserialization from XML.
 */
public class WrapperHandlingDeserializer
    extends DelegatingDeserializer
{
    /**
     * (Simple) Names of properties, for which virtual wrapping is needed
     * to compensate: these are so-called 'unwrapped' XML lists where property
     * name is used for elements, and not as List markers.
     */
    protected final Set<String> _namesToWrap;

    protected final JavaType _type;

    // @since 2.12
    protected final boolean _caseInsensitive;

    /**
     * [dataformat-xml#608] Optional "XML Text" property for handling VALUE_STRING
     * tokens when the bean has {@code @JacksonXmlText} alongside other element properties.
     *
     * @since 3.2
     */
    protected final SettableBeanProperty _xmlTextProperty;

    /**
     * @since 3.2
     */
    protected final int _xmlTextPropertyIndex;

    protected final ValueInstantiator _valueInstantiator;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate) {
        this(delegate, (Set<String>) null, null);
    }

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate, Set<String> namesToWrap)
    {
        this(delegate, namesToWrap, null);
    }

    /**
     * [dataformat-xml#608] Constructor that accepts a text property for handling
     * VALUE_STRING tokens when bean has {@code @JacksonXmlText} plus other element properties.
     *
     * @since 3.2
     */
    public WrapperHandlingDeserializer(BeanDeserializerBase delegate,
            SettableBeanProperty xmlTextProperty)
    {
        this(delegate, null, xmlTextProperty);
    }

    /**
     * @since 3.2
     */
    public WrapperHandlingDeserializer(BeanDeserializerBase delegate, Set<String> namesToWrap,
            SettableBeanProperty xmlTextProperty)
    {
        super(delegate);
        _namesToWrap = namesToWrap;
        _type = delegate.getValueType();
        _caseInsensitive = delegate.isCaseInsensitive();
        _xmlTextProperty = xmlTextProperty;
        _xmlTextPropertyIndex = (xmlTextProperty != null) ? xmlTextProperty.getPropertyIndex() : -1;
        _valueInstantiator = delegate.getValueInstantiator();
    }

    /*
    /**********************************************************************
    /* Abstract method implementations
    /**********************************************************************
     */

    @Override
    protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee0) {
        // [dataformat-xml#762]: need to support creating new instance when delegatee
        // changes, e.g. when @JsonUnwrapped triggers creation of unwrapping deserializer
        if (!(newDelegatee0 instanceof BeanDeserializerBase newDelegatee)) {
            throw new IllegalArgumentException("Cannot change delegate to be of type "
                    +newDelegatee0.getClass().getName());
        }
        return new WrapperHandlingDeserializer(newDelegatee, _namesToWrap, _xmlTextProperty);
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property)
    {
        JavaType vt = _type;
        if (vt == null) {
            vt = ctxt.constructType(_delegatee.handledType());
        }
        ValueDeserializer<?> del = ctxt.handleSecondaryContextualization(_delegatee, property, vt);
        BeanDeserializerBase newDelegatee = _verifyDeserType(del);

        // [dataformat-xml#608] Re-resolve text property from contextualized delegatee
        SettableBeanProperty newTextProp = (_xmlTextPropertyIndex >= 0)
                ? newDelegatee.findProperty(_xmlTextPropertyIndex) : null;

        // Let's go through the properties now...
        Iterator<SettableBeanProperty> it = newDelegatee.properties();
        HashSet<String> unwrappedNames = null;
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            // First things first: only consider array/Collection types
            // (not perfect check, but simplest reasonable check)
            JavaType type = prop.getType();
            if (!TypeUtil.isIndexedType(type)) {
                continue;
            }
            PropertyName wrapperName = prop.getWrapperName();
            // skip anything with wrapper (should work as is)
            if ((wrapperName != null) && (wrapperName != PropertyName.NO_NAME)) {
                continue;
            }
            if (unwrappedNames == null) {
                unwrappedNames = new HashSet<String>();
            }
            // not optimal; should be able to use PropertyName...
            unwrappedNames.add(prop.getName());
            for (PropertyName alias : prop.findAliases(ctxt.getConfig())) {
                unwrappedNames.add(alias.getSimpleName());
            }
        }
        // Ok: if nothing to take care of, and no text property to handle,
        // just return the delegatee...
        if (unwrappedNames == null && newTextProp == null) {
            return newDelegatee;
        }
        // Otherwise, create the thing that can deal with virtual wrapping
        // and/or text property handling
        return new WrapperHandlingDeserializer(newDelegatee, unwrappedNames, newTextProp);
    }

    /*
    /**********************************************************************
    /* Overridden deserialization methods
    /**********************************************************************
     */

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException
    {
        // [dataformat-xml#608]: Handle VALUE_STRING when we have a text property
        if (_xmlTextProperty != null && p.currentToken() == JsonToken.VALUE_STRING) {
            return _deserializeFromXmlText(p, ctxt);
        }
        _configureParser(p);
        return _delegatee.deserialize(p,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue)
        throws JacksonException
    {
        // [dataformat-xml#608]: Handle VALUE_STRING when we have a text property
        if (_xmlTextProperty != null && p.currentToken() == JsonToken.VALUE_STRING) {
            _xmlTextProperty.deserializeAndSet(p, ctxt, intoValue);
            return intoValue;
        }
        _configureParser(p);
        return ((ValueDeserializer<Object>)_delegatee).deserialize(p, ctxt, intoValue);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
        throws JacksonException
    {
        _configureParser(p);
        return _delegatee.deserializeWithType(p, ctxt, typeDeserializer);
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    /**
     * [dataformat-xml#608]: When the parser sees a bare VALUE_STRING but the target type
     * is a bean with {@code @JacksonXmlText} plus other element properties, create the
     * bean and set the text property.
     *
     * @since 3.2
     */
    private Object _deserializeFromXmlText(JsonParser p, DeserializationContext ctxt)
        throws JacksonException
    {
        if (_valueInstantiator.canCreateUsingDefault()) {
            Object bean = _valueInstantiator.createUsingDefault(ctxt);
            _xmlTextProperty.deserializeAndSet(p, ctxt, bean);
            return bean;
        }
        // No default constructor (e.g. records): synthesize object tokens
        // so the delegate can use property-based creators
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

    @SuppressWarnings("resource")
    protected final void _configureParser(JsonParser p) throws JacksonException
    {
        if (_namesToWrap == null) {
            return;
        }
        // 05-Sep-2019, tatu: May get XML parser, except for case where content is
        //   buffered. In that case we may still have access to real parser if we
        //   are lucky (like in [dataformat-xml#242])
        // 15-Mar-2026, tatu: [dataformat-xml#455] Check for ElementWrappable at
        //   each delegation level, not just at the innermost parser. This handles
        //   the case where XmlTokenBuffer wraps the TokenBuffer.Parser with an
        //   ElementWrappable delegate during polymorphic type resolution.
        while (p instanceof JsonParserDelegate jpd) {
            if (p instanceof ElementWrappable) {
                break;
            }
            p = jpd.delegate();
        }
        if (p instanceof ElementWrappable ew) {
            // 03-May-2021, tatu: as per [dataformat-xml#469] there are special
            //   cases where we get String token to represent XML empty element.
            //   If so, need to refrain from adding wrapping as that would
            //   override parent settings
            JsonToken t = p.currentToken();
            if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY
                    // 12-Dec-2021, tatu: [dataformat-xml#490] There seems to be
                    //    cases here (similar to regular JSON) where leading START_OBJECT
                    //    is consumed during buffering, so need to consider that too
                    //    it seems (just hope we are at correct level and not off by one...)
                    || t == JsonToken.PROPERTY_NAME) {
                ew.addVirtualWrapping(_namesToWrap, _caseInsensitive);
            }
        }
    }

    protected BeanDeserializerBase _verifyDeserType(ValueDeserializer<?> deser)
    {
        if (!(deser instanceof BeanDeserializerBase bdb)) {
            throw new IllegalArgumentException("Can not change delegate to be of type "
                    +deser.getClass().getName());
        }
        return bdb;
    }
}
