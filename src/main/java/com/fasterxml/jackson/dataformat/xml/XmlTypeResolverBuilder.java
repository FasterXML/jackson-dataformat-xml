package com.fasterxml.jackson.dataformat.xml;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * Custom specialization of {@link StdTypeResolverBuilder}; needed so that
 * type id property name can be modified as necessary to make it legal
 * XML element or attribute name.
 */
public class XmlTypeResolverBuilder extends StdTypeResolverBuilder
{
    public XmlTypeResolverBuilder(JsonTypeInfo.Value typeInfo)
    {
        super(typeInfo);
        if (_typeProperty != null) {
            _typeProperty = StaxUtil.sanitizeXmlTypeName(_typeProperty);
        }
    }

    @Override
    public StdTypeResolverBuilder init(JsonTypeInfo.Value settings, TypeIdResolver idRes)
    {
        super.init(settings, idRes);
        if (_typeProperty != null) {
            _typeProperty = StaxUtil.sanitizeXmlTypeName(_typeProperty);
        }
        return this;
    }

    /*
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
    */

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
            return new XmlClassNameIdResolver(baseType, subTypeValidator(ctxt));
        case MINIMAL_CLASS:
            return new XmlMinimalClassNameIdResolver(baseType, subTypeValidator(ctxt));
        default:
        }
        return super.idResolver(ctxt, baseType, subtypeValidator, subtypes, forSer, forDeser);
    }

    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    /**
     * Helper method for encoding regular Java class name in form that
     * can be used as XML element name.
     */
    protected static String encodeXmlClassName(String className)
    {
        /* For now, let's just replace '$'s with double dots...
         * Perhaps make configurable in future?
         */
        int ix = className.lastIndexOf('$');
        if (ix >= 0) {
            StringBuilder sb = new StringBuilder(className);
            do {
                sb.replace(ix, ix+1, "..");
                ix = className.lastIndexOf('$', ix-1);
            } while (ix >= 0);
            className = sb.toString();
        }
        return className;
    }

    /**
     * Helper method for decoding "XML safe" Java class name back into
     * actual class name
     */
    protected static String decodeXmlClassName(String className)
    {
        int ix = className.lastIndexOf("..");
        if (ix >= 0) {
            StringBuilder sb = new StringBuilder(className);
            do {
                sb.replace(ix, ix+2, "$");
                ix = className.lastIndexOf("..", ix-1);
            } while (ix >= 0);
            className = sb.toString();
        }
        return className;
    }

    /*
    /**********************************************************************
    /* Customized class name handlers
    /**********************************************************************
     */

    protected static class XmlClassNameIdResolver
        extends ClassNameIdResolver
    {
        public XmlClassNameIdResolver(JavaType baseType, PolymorphicTypeValidator ptv)
        {
            super(baseType, ptv);
        }

        @Override
        public String idFromValue(DatabindContext ctxt, Object value)
        {
            return encodeXmlClassName(super.idFromValue(ctxt, value));
        }

        @Override
        public JavaType typeFromId(DatabindContext ctxt, String id) throws JacksonException {
            return super.typeFromId(ctxt, decodeXmlClassName(id));
        }
    }

    protected static class XmlMinimalClassNameIdResolver
        extends MinimalClassNameIdResolver
    {
        public XmlMinimalClassNameIdResolver(JavaType baseType, PolymorphicTypeValidator ptv)
        {
            super(baseType, ptv);
        }

        @Override
        public String idFromValue(DatabindContext ctxt, Object value)
        {
            return encodeXmlClassName(super.idFromValue(ctxt, value));
        }

        @Override
        public JavaType typeFromId(DatabindContext ctxt, String id) throws JacksonException {
            return super.typeFromId(ctxt, decodeXmlClassName(id));
        }
    }
}
