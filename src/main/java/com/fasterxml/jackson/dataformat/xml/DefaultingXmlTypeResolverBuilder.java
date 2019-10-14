package com.fasterxml.jackson.dataformat.xml;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;
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
    extends DefaultTypeResolverBuilder
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public DefaultingXmlTypeResolverBuilder(PolymorphicTypeValidator ptv, DefaultTyping applicability,
            JsonTypeInfo.As includeAs) {
        super(ptv, applicability, includeAs);
    }

    public DefaultingXmlTypeResolverBuilder(PolymorphicTypeValidator ptv,
            DefaultTyping applicability,
            String propertyName) {
        super(ptv, applicability, propertyName);
    }

    /*
    /**********************************************************************
    /* Methods copied from `XmlTypeResolverBuilder`
    /**********************************************************************
     */

    @Override
    public StdTypeResolverBuilder init(JsonTypeInfo.Value settings, TypeIdResolver idRes)
    {
        super.init(settings, idRes);
        if (_typeProperty != null) {
            _typeProperty = StaxUtil.sanitizeXmlTypeName(_typeProperty);
        }
        return this;
    }

    @Override
    protected TypeIdResolver idResolver(DatabindContext ctxt,
            JavaType baseType, PolymorphicTypeValidator subtypeValidator,
            Collection<NamedType> subtypes, boolean forSer, boolean forDeser)
    {
        if (_customIdResolver != null) {
            return _customIdResolver;
        }
        // Only override handlers of class, minimal class; name is good as is
        switch (_idType) {
        case CLASS:
            return new XmlTypeResolverBuilder.XmlClassNameIdResolver(baseType,
                    subTypeValidator(ctxt));
        case MINIMAL_CLASS:
            return new XmlTypeResolverBuilder.XmlMinimalClassNameIdResolver(baseType,
                    subTypeValidator(ctxt));
        default:
        }
        return super.idResolver(ctxt, baseType, subtypeValidator, subtypes, forSer, forDeser);
    }
}
