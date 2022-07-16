package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.jsontype.TypeResolverBuilder;
import tools.jackson.databind.jsontype.TypeResolverProvider;

/**
 * @since 3.0 (replacement for earlier AnnotationIntrospector-based approach in 2.x)
 */
public class XmlTypeResolverProvider
    extends TypeResolverProvider
{
    private static final long serialVersionUID = 3L;

    @Override
    protected TypeResolverBuilder<?> _constructStdTypeResolverBuilder(MapperConfig<?> config,
            JsonTypeInfo.Value typeInfo, JavaType baseType) {
        return new XmlTypeResolverBuilder(typeInfo);
    }
}
