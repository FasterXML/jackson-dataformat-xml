package com.fasterxml.jackson.dataformat.xml;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.dataformat.xml.annotate.*;


/**
 * Extension of {@link JacksonAnnotationIntrospector} that is needed to support
 * additional xml-specific annotation that Jackson provides. Note, however, that
 * there is no JAXB annotation support here; that is provided with
 * separate introspector (see {@link org.codehaus.jackson.xc.JaxbAnnotationIntrospector}).
 */
public class JacksonXmlAnnotationIntrospector
    extends JacksonAnnotationIntrospector
    implements XmlAnnotationIntrospector
{    
    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector
    /**********************************************************************
     */

    @Override
    public Boolean isOutputAsAttribute(Annotated ann)
    {
        JacksonXmlProperty prop = ann.getAnnotation(JacksonXmlProperty.class);
        if (prop != null) {
            return prop.isAttribute() ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }

    @Override
    public String findNamespace(Annotated ann)
    {
        JacksonXmlProperty prop = ann.getAnnotation(JacksonXmlProperty.class);
        if (prop != null) {
            return prop.namespace();
        }
        return null;
    }

    @Override
    public QName findWrapperElement(Annotated ann)
    {
        JacksonXmlElementWrapper w = ann.getAnnotation(JacksonXmlElementWrapper.class);
        if (w != null) {
            return new QName(w.namespace(), w.localName());
        }
        return null;
    }

    @Override
    public QName findRootElement(Annotated ann)
    {
        JacksonXmlRootElement root = ann.getAnnotation(JacksonXmlRootElement.class);
        if (root != null) {
            return new QName(root.namespace(), root.localName());
        }
        return null;
    }
    
    /*
    /**********************************************************************
    /* Overrides for name, property detection
    /**********************************************************************
     */
    
    @Override
    public String findSerializationName(AnnotatedField af)
    {
        JacksonXmlProperty pann = af.getAnnotation(JacksonXmlProperty.class);
        if (pann != null) {
            return pann.localName();
        }
        return super.findSerializationName(af);
    }

    @Override
    public String findSerializationName(AnnotatedMethod am)
    {
        JacksonXmlProperty pann = am.getAnnotation(JacksonXmlProperty.class);
        if (pann != null) {
            return pann.localName();
        }
        return super.findSerializationName(am);
    }

    @Override
    public String findDeserializationName(AnnotatedField af)
    {
        JacksonXmlProperty pann = af.getAnnotation(JacksonXmlProperty.class);
        if (pann != null) {
            return pann.localName();
        }
        return super.findDeserializationName(af);
    }

    @Override
    public String findDeserializationName(AnnotatedParameter ap)
    {
        JacksonXmlProperty pann = ap.getAnnotation(JacksonXmlProperty.class);
        // can not return empty String here, so:
        if (pann != null) {
            String name = pann.localName();
            if (name.length() > 0) {
                return name;
            }
        }
        return super.findDeserializationName(ap);
    }

    /*
    /**********************************************************************
    /* Overrides for non-public helper methods
    /**********************************************************************
     */

    /**
     * We will override this method so that we can return instance
     * that cleans up type id property name to be a valid xml name.
     */
    @Override
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder()
    {
        return new XmlTypeResolverBuilder();
    }
}

