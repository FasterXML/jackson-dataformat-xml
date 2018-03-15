package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.TypeResolverProvider;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

public class XmlTypeResolverProvider
    extends TypeResolverProvider
{
    private static final long serialVersionUID = 3L;

    @Override
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder(JsonTypeInfo.Value typeInfo) {
        return new XmlTypeResolverBuilder(typeInfo);
    }
}
