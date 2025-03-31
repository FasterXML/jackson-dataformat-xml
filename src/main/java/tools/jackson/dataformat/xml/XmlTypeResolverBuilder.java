package tools.jackson.dataformat.xml;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.core.JacksonException;

import tools.jackson.databind.*;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.TypeIdResolver;
import tools.jackson.databind.jsontype.impl.ClassNameIdResolver;
import tools.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import tools.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import tools.jackson.dataformat.xml.util.StaxUtil;

/**
 * Custom specialization of {@link StdTypeResolverBuilder}; needed so that
 * type id property name can be modified as necessary to make it legal
 * XML element or attribute name.
 *<p>
 * NOTE: Since 2.17, property name cleansing only applied to default
 * names (like {@code "@class"} and {@code "@type"}) but not to explicitly
 * specified ones (where caller presumably knows what to do).
 */
public class XmlTypeResolverBuilder extends StdTypeResolverBuilder
{
    public XmlTypeResolverBuilder(JsonTypeInfo.Value typeInfo)
    {
        super(typeInfo);
    }

    @Override
    protected String _propName(String propName, JsonTypeInfo.Id idType) {
        // 30-Jan-2024, tatu: Before 2.17 we used to indiscriminately cleanse
        //   property name always; with 2.17+ only default ones
        if (propName == null || propName.isEmpty()) {
            propName = StaxUtil.sanitizeXmlTypeName(idType.getDefaultPropertyName());
        } else {
            // ... alas, there's... a "feature" (read: bug) in `JsonTypeInfo.Value` construction
            // which will automatically apply default property name if no explicit property
            // name specific. This means we don't really know if default is being used.
            // But let's assume that if "propName.equals(defaultPropName)" this is the case.
            if (propName.equals(idType.getDefaultPropertyName())) {
                propName = StaxUtil.sanitizeXmlTypeName(propName);
            }
        }
        return propName;
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
            return new XmlClassNameIdResolver(baseType, subtypes,
                    subTypeValidator(ctxt));
        case MINIMAL_CLASS:
            return new XmlMinimalClassNameIdResolver(baseType, subtypes,
                    subTypeValidator(ctxt));
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
        private static final long serialVersionUID = 3L;

        public XmlClassNameIdResolver(JavaType baseType, Collection<NamedType> subtypes,
                PolymorphicTypeValidator ptv)
        {
            super(baseType, subtypes, ptv);
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
        private static final long serialVersionUID = 3L;

        public XmlMinimalClassNameIdResolver(JavaType baseType, Collection<NamedType> subtypes,
                PolymorphicTypeValidator ptv)
        {
            super(baseType, subtypes, ptv);
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
