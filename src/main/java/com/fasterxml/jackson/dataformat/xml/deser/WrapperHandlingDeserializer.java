package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.Iterator;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Delegating deserializer whose only function is to handle case of
 * "unwrapped" List/array deserialization from XML.
 */
public class WrapperHandlingDeserializer
    extends DelegatingDeserializer
{
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate)
    {
        super(delegate);
    }

    /*
    /**********************************************************************
    /* Abstract method implementations
    /**********************************************************************
     */
    
    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
        return new WrapperHandlingDeserializer(_verifyDeserType(newDelegatee));
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
        final AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            AnnotatedMember acc = prop.getMember();
            PropertyName wrapperName = (acc == null) ? null : intr.findWrapperName(acc);
//System.out.println("Prop '"+prop.getName()+"', wrapper -> "+wrapperName);

        }
//        System.out.println("DEBUG: /createContextual");
        
        /*
        if (newDelegatee == _delegatee) {
            return this;
        }
        return newDelegatingInstance(newDelegatee);
        */

        return newDelegatee;
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
