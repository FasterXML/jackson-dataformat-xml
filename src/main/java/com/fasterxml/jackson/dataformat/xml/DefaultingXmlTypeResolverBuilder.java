package com.fasterxml.jackson.dataformat.xml;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * Sub-class of {@code StdTypeResolverBuilder} specifically used with
 * Default Typing.
 *<p>
 * Composition/sub-classing gets quite tricky here: ideally we would just
 * extend {@link XmlTypeResolverBuilder} but unfortunately inheritance hierarchy
 * does not allow this.
 *
 * @since 2.10
 */
public class DefaultingXmlTypeResolverBuilder
    extends ObjectMapper.DefaultTypeResolverBuilder
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public DefaultingXmlTypeResolverBuilder(DefaultTyping t, PolymorphicTypeValidator ptv) {
        super(t, ptv);
    }

    /*
    /**********************************************************************
    /* Methods copied from `XmlTypeResolverBuilder`
    /**********************************************************************
     */

    @Override
    public StdTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idRes)
    {
        super.init(idType, idRes);
        if (_typeProperty != null) {
            _typeProperty = StaxUtil.sanitizeXmlTypeName(_typeProperty);
        }
        return this;
    }

    @Override
    public StdTypeResolverBuilder typeProperty(String typeIdPropName)
    {
        // ok to have null/empty; will restore to use defaults
        if (typeIdPropName == null || typeIdPropName.length() == 0) {
            typeIdPropName = _idType.getDefaultPropertyName();
        }
        _typeProperty = StaxUtil.sanitizeXmlTypeName(typeIdPropName);
        return this;
    }

    @Override
    protected TypeIdResolver idResolver(MapperConfig<?> config,
            JavaType baseType, PolymorphicTypeValidator subtypeValidator,
            Collection<NamedType> subtypes, boolean forSer, boolean forDeser)
    {
        if (_customIdResolver != null) {
            return _customIdResolver;
        }
        // Only override handlers of class, minimal class; name is good as is
        switch (_idType) {
        case CLASS:
            return new XmlTypeResolverBuilder.XmlClassNameIdResolver(baseType, config.getTypeFactory(),
                    subTypeValidator(config));
        case MINIMAL_CLASS:
            return new XmlTypeResolverBuilder.XmlMinimalClassNameIdResolver(baseType, config.getTypeFactory(),
                    subTypeValidator(config));
        default:
        }
        return super.idResolver(config, baseType, subtypeValidator, subtypes, forSer, forDeser);
    }
}
