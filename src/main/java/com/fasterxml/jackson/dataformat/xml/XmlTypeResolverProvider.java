package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.TypeResolverProvider;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

/**
 * @since 3.0 (replacement for earlier AnnotationIntrospector-based approach in 2.x)
 */
public class XmlTypeResolverProvider
    extends TypeResolverProvider
{
    private static final long serialVersionUID = 3L;

    @Override
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder(MapperConfig<?> config,
            JsonTypeInfo.Value typeInfo, JavaType baseType) {
        return new XmlTypeResolverBuilder(typeInfo);
    }
}
