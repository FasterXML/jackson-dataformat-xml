package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;

/**
 * XML-specific {@link DeserializationContext} needed to override certain
 * handlers.
 *
 * @since 2.12
 */
public class XmlDeserializationContext
    extends DefaultDeserializationContext
{
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor for a blueprint object, which will use the standard
     * {@link DeserializerCache}, given factory.
     */
    public XmlDeserializationContext(DeserializerFactory df) {
        super(df, null);
    }

    private XmlDeserializationContext(XmlDeserializationContext src,
            DeserializationConfig config, JsonParser p, InjectableValues values) {
        super(src, config, p, values);
    }

    private XmlDeserializationContext(XmlDeserializationContext src) { super(src); }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializerFactory factory) {
        super(src, factory);
    }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializationConfig config) {
        super(src, config);
    }

    @Override
    public XmlDeserializationContext copy() {
        return new XmlDeserializationContext(this);
    }

    @Override
    public DefaultDeserializationContext createInstance(DeserializationConfig config,
            JsonParser p, InjectableValues values) {
        return new XmlDeserializationContext(this, config, p, values);
    }

    @Override
    public DefaultDeserializationContext createDummyInstance(DeserializationConfig config) {
        // need to be careful to create non-blue-print instance
        return new XmlDeserializationContext(this, config);
    }

    @Override
    public DefaultDeserializationContext with(DeserializerFactory factory) {
        return new XmlDeserializationContext(this, factory);
    }
}
