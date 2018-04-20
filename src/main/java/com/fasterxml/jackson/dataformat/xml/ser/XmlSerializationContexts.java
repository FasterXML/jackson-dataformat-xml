package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.TokenStreamFactory;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.GeneratorSettings;
import com.fasterxml.jackson.databind.cfg.SerializationContexts;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
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

    protected final XmlRootNameLookup _rootNameLookup;
    
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
    public DefaultSerializerProvider createContext(SerializationConfig config,
            GeneratorSettings genSettings) {
        return new XmlSerializerProvider(_streamFactory,
                _serializerCache,
                config, genSettings, _serializerFactory,
                _rootNameLookup);
    }

    @Override
    public SerializationContexts forMapper(ObjectMapper mapper,
            TokenStreamFactory tsf, SerializerFactory serializerFactory,
            SerializerCache cache) {
        return new XmlSerializationContexts(tsf, serializerFactory, cache,
                new XmlRootNameLookup());
    }
}
