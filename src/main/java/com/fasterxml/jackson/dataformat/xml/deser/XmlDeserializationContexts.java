package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.TokenStreamFactory;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.cfg.DeserializationContexts;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;

public class XmlDeserializationContexts
    extends DeserializationContexts
{
    private static final long serialVersionUID = 3L;

    public XmlDeserializationContexts() { super(); }
    public XmlDeserializationContexts(TokenStreamFactory tsf,
            DeserializerFactory serializerFactory, DeserializerCache cache) {
        super(tsf, serializerFactory, cache);
    }

    @Override
    public DeserializationContexts forMapper(Object mapper,
            TokenStreamFactory tsf, DeserializerFactory serializerFactory,
            DeserializerCache cache) {
        return new XmlDeserializationContexts(tsf, serializerFactory, cache);
    }

    @Override
    public DefaultDeserializationContext createContext(DeserializationConfig config,
            FormatSchema schema, InjectableValues injectables) {
        return new XmlDeserializationContext(_streamFactory,
                _deserializerFactory, _cache,
                config, schema, injectables);
    }
}
