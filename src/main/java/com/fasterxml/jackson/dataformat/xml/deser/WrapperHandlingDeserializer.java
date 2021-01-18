package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.*;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;

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
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate) {
        this(delegate, null);
    }

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate, Set<String> namesToWrap)
    {
        super(delegate);
        _namesToWrap = namesToWrap;
        _type = delegate.getValueType();
        _caseInsensitive = delegate.isCaseInsensitive();
    }

    /*
    /**********************************************************************
    /* Abstract method implementations
    /**********************************************************************
     */

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee0) {
        // default not enough, as we may need to create a new wrapping deserializer
        // even if delegatee does not change
        throw new IllegalStateException("Internal error: should never get called");
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property)
        throws JsonMappingException
    {
        JavaType vt = _type;
        if (vt == null) {
            vt = ctxt.constructType(_delegatee.handledType());
        }
        JsonDeserializer<?> del = ctxt.handleSecondaryContextualization(_delegatee, property, vt);
        BeanDeserializerBase newDelegatee = _verifyDeserType(del);
        
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
        }
        // Ok: if nothing to take care of, just return the delegatee...
        if (unwrappedNames == null) {
            return newDelegatee;
        }
        // Otherwise, create the thing that can deal with virtual wrapping
        return new WrapperHandlingDeserializer(newDelegatee, unwrappedNames);
    }

    /*
    /**********************************************************************
    /* Overridden deserialization methods
    /**********************************************************************
     */

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException
    {
        _configureParser(p);
        return _delegatee.deserialize(p,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue)
        throws JacksonException
    {
        _configureParser(p);
        return ((JsonDeserializer<Object>)_delegatee).deserialize(p, ctxt, intoValue);
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

    @SuppressWarnings("resource")
    protected final void _configureParser(JsonParser p) throws JacksonException
    {
        // 05-Sep-2019, tatu: May get XML parser, except for case where content is
        //   buffered. In that case we may still have access to real parser if we
        //   are lucky (like in [dataformat-xml#242])
        while (p instanceof JsonParserDelegate) {
            p = ((JsonParserDelegate) p).delegate();
        }
        if (p instanceof FromXmlParser) {
            ((FromXmlParser) p).addVirtualWrapping(_namesToWrap, _caseInsensitive);
        }
    }
    
    protected BeanDeserializerBase _verifyDeserType(JsonDeserializer<?> deser)
    {
        if (!(deser instanceof BeanDeserializerBase)) {
            throw new IllegalArgumentException("Can not change delegate to be of type "
                    +deser.getClass().getName());
        }
        return (BeanDeserializerBase) deser;
    }
}
