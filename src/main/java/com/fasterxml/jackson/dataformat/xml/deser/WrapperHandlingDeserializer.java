package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    protected final Set<String> _namesToWrap;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate) {
        this(delegate, null);
    }

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate,
            Set<String> namesToWrap) {
        super(delegate);
        _namesToWrap = namesToWrap;
    }
    
    /*
    /**********************************************************************
    /* Abstract method implementations
    /**********************************************************************
     */
    
    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
        return new WrapperHandlingDeserializer(_verifyDeserType(newDelegatee),
                _namesToWrap);
    }
    
    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    @Override
    protected JsonDeserializer<?> _createContextual(DeserializationContext ctxt,
            BeanProperty property, JsonDeserializer<?> newDelegatee0)
    {
        BeanDeserializerBase newDelegatee = _verifyDeserType(newDelegatee0);

//System.out.println("DEBUG: createContextual!");
        
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
    public Object deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        _configureParser(jp);
        return _delegatee.deserialize(jp,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt,
            Object intoValue)
        throws IOException, JsonProcessingException
    {
        _configureParser(jp);
        return ((JsonDeserializer<Object>)_delegatee).deserialize(jp, ctxt, intoValue);
    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        _configureParser(jp);
        return _delegatee.deserializeWithType(jp, ctxt, typeDeserializer);
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected void _configureParser(JsonParser jp)
        throws IOException, JsonProcessingException
    {
        ((FromXmlParser) jp).addVirtualWrapping(_namesToWrap);
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
