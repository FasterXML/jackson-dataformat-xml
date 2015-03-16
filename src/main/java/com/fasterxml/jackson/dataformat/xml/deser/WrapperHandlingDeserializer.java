package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
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
    private static final long serialVersionUID = 1L;

    /**
     * (Simple) Names of properties, for which virtual wrapping is needed
     * to compensate: these are so-called 'unwrapped' XML lists where property
     * name is used for elements, and not as List markers.
     */
    protected final Set<String> _namesToWrap;

    protected final JavaType _type;
    
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
        // !!! 16-Jan-2015, tatu: TODO: change to be like so in 2.6.0 -- leaving
        //    out for 2.5 just to increase compatibility slightly with 2.4 databind
        /*
        JavaType vt = _type;
        if (vt == null) {
            vt = ctxt.constructType(_delegatee.handledType());
        }
        JsonDeserializer<?> del = ctxt.handleSecondaryContextualization(_delegatee, property, vt);
        */

        JsonDeserializer<?> del = ctxt.handleSecondaryContextualization(_delegatee, property, _type);
        BeanDeserializerBase newDelegatee = _verifyDeserType(del);
        
        // Let's go through the properties now...
        Iterator<SettableBeanProperty> it = newDelegatee.properties();
        HashSet<String> unwrappedNames = null;
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            /* First things first: only consider array/Collection types
             * (not perfect check, but simplest reasonable check)
             */
            JavaType type = prop.getType();
            if (!TypeUtil.isIndexedType(type)) {
                continue;
            }
            PropertyName wrapperName = prop.getWrapperName();
            // skip anything with wrapper (should work as is)
            if (wrapperName != null && wrapperName != PropertyName.NO_NAME) {
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
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        _configureParser(jp);
        return _delegatee.deserialize(jp,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt,
            Object intoValue) throws IOException
    {
        _configureParser(jp);
        return ((JsonDeserializer<Object>)_delegatee).deserialize(jp, ctxt, intoValue);
    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer) throws IOException
    {
        _configureParser(jp);
        return _delegatee.deserializeWithType(jp, ctxt, typeDeserializer);
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected final void _configureParser(JsonParser jp) throws IOException
    {
        /* 19-Aug-2013, tatu: Although we should not usually get called with
         *   parser of other types, there are some cases where this may happen:
         *   specifically, during structural value conversions.
         */
        if (jp instanceof FromXmlParser) {
            ((FromXmlParser) jp).addVirtualWrapping(_namesToWrap);
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
