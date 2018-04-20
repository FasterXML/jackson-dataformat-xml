package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.TokenStreamFactory;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.GeneratorSettings;
import com.fasterxml.jackson.databind.cfg.SerializationContexts;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
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
    
    public XmlSerializationContexts(TokenStreamFactory tsf) {
        super(tsf);
        _rootNameLookup = new XmlRootNameLookup();
    }

    @Override
    public SerializationContexts snapshot() {
        return new XmlSerializationContexts(_streamFactory);
    }

    @Override
    public DefaultSerializerProvider createContext(SerializationConfig config,
            GeneratorSettings genSettings, SerializerFactory serFactory) {
        return new XmlSerializerProvider(_streamFactory,
                _serializerCache,
                config, genSettings, serFactory,
                _rootNameLookup);
    }
}
