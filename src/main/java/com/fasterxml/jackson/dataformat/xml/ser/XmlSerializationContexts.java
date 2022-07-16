package com.fasterxml.jackson.dataformat.xml.ser;

import tools.jackson.core.TokenStreamFactory;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.GeneratorSettings;
import tools.jackson.databind.cfg.SerializationContexts;
import tools.jackson.databind.ser.SerializationContextExt;
import tools.jackson.databind.ser.SerializerCache;
import tools.jackson.databind.ser.SerializerFactory;

import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * Factory implementation we need to provide custom {@link SerializerProvider}
 * variants.
 *
 * @since 3.0
 */
public class XmlSerializationContexts extends SerializationContexts
{
    private static final long serialVersionUID = 3L;

    // // Similar to base class, we do NOT want (or need) to JDK serialize anything,
    // // other than ensure right class is unthawed. This because serialization of
    // // ObjectMapper retains MapperState, which has "empty" instance of this object
    // // but will call `forMapper(...)` when JDK deserialized, setting up fields.
    
    protected final transient XmlRootNameLookup _rootNameLookup;
    
    public XmlSerializationContexts() {
        _rootNameLookup = null;
    }

    protected  XmlSerializationContexts(TokenStreamFactory tsf,
            SerializerFactory serializerFactory, SerializerCache cache,
            XmlRootNameLookup roots) {
        super(tsf, serializerFactory, cache);
        _rootNameLookup = roots;
    }

    @Override
    public SerializationContexts forMapper(Object mapper,
            TokenStreamFactory tsf, SerializerFactory serializerFactory,
            SerializerCache cache) {
        return new XmlSerializationContexts(tsf, serializerFactory, cache,
                new XmlRootNameLookup());
    }

    @Override
    public SerializationContextExt createContext(SerializationConfig config,
            GeneratorSettings genSettings) {
        return new XmlSerializerProvider(_streamFactory,
                config, genSettings, _serializerFactory, _cache,
                _rootNameLookup);
    }
}
