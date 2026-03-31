package tools.jackson.dataformat.xml.deser;

import tools.jackson.core.FormatSchema;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.InjectableValues;
import tools.jackson.databind.cfg.DeserializationContexts;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.deser.DeserializerCache;
import tools.jackson.databind.deser.DeserializerFactory;
import tools.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlDeserializationContexts
    extends DeserializationContexts
{
    private static final long serialVersionUID = 3L;

    protected final transient XmlRootNameLookup _rootNameLookup;

    public XmlDeserializationContexts() {
        _rootNameLookup = null;
    }

    protected XmlDeserializationContexts(TokenStreamFactory tsf,
            DeserializerFactory serializerFactory, DeserializerCache cache,
            XmlRootNameLookup roots) {
        super(tsf, serializerFactory, cache);
        _rootNameLookup = roots;
    }

    @Override
    public DeserializationContexts forMapper(Object mapper,
            TokenStreamFactory tsf, DeserializerFactory serializerFactory,
            DeserializerCache cache) {
        return new XmlDeserializationContexts(tsf, serializerFactory, cache,
                new XmlRootNameLookup());
    }

    @Override
    public DeserializationContextExt createContext(DeserializationConfig config,
            FormatSchema schema, InjectableValues injectables) {
        return new XmlDeserializationContext(_streamFactory,
                _deserializerFactory, _cache,
                config, schema, injectables,
                _rootNameLookup);
    }
}
