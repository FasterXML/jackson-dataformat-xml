package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;

/**
 * {@link ValueInstantiator} implementation needed mostly to support
 * creating "empty" instance (or {@code null}) of POJOs from String
 * value consisting of all whitespace -- something that happens with
 * indentation.
 *
 * @since 2.12
 */
public class XmlBeanInstantiator
    extends ValueInstantiator.Delegating
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected final JavaType _type;

    protected final boolean _canCreateDefault;

    public XmlBeanInstantiator(JavaType type, ValueInstantiator base) {
        super(base);
        _type = type;
        _canCreateDefault = base.canCreateUsingDefault();
    }

    @Override
    public boolean canInstantiate() { return true; }

    @Override
    public boolean canCreateFromString() {
        return true;
    }

    @Override
    public Object createFromString(DeserializationContext ctxt, String value)
            throws IOException
    {
        if (_canCreateDefault) {
            if (value.isEmpty() || value.trim().isEmpty()) {
                return delegate().createUsingDefault(ctxt);
            }
            // Should we try to give more meaningful error otherwise?
        }
        return delegate().createFromString(ctxt, value);
    }

    public static class Provider extends ValueInstantiators.Base
        implements java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public ValueInstantiator findValueInstantiator(DeserializationConfig config,
                BeanDescription beanDesc, ValueInstantiator defaultInstantiator)
        {
            // let's avoid custom ones?
            if (defaultInstantiator instanceof StdValueInstantiator) {
                return new XmlBeanInstantiator(beanDesc.getType(), defaultInstantiator);
            }
            return defaultInstantiator;
        }
    }
}
