package com.fasterxml.jackson.dataformat.xml.deser;

import tools.jackson.core.FormatSchema;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.InjectableValues;
import tools.jackson.databind.cfg.DeserializationContexts;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.deser.DeserializerCache;
import tools.jackson.databind.deser.DeserializerFactory;

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
    public DeserializationContextExt createContext(DeserializationConfig config,
            FormatSchema schema, InjectableValues injectables) {
        return new XmlDeserializationContext(_streamFactory,
                _deserializerFactory, _cache,
                config, schema, injectables);
    }
}
