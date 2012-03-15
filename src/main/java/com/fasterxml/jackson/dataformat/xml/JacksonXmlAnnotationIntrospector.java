package com.fasterxml.jackson.dataformat.xml;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import com.fasterxml.jackson.dataformat.xml.annotation.*;

/**
 * Extension of {@link JacksonAnnotationIntrospector} that is needed to support
 * additional xml-specific annotation that Jackson provides. Note, however, that
 * there is no JAXB annotation support here; that is provided with
 * separate introspector (see
 * {@link com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector}).
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
        String name = _findXmlName(af);
        if (name != null) {
            return name;
        }
        return super.findSerializationName(af);
    }

    @Override
    public String findSerializationName(AnnotatedMethod am)
    {
        String name = _findXmlName(am);
        if (name != null) {
            return name;
        }
        return super.findSerializationName(am);
    }

    @Override
    public String findDeserializationName(AnnotatedField af)
    {
        String name = _findXmlName(af);
        if (name != null) {
            return name;
        }
        return super.findDeserializationName(af);
    }

    @Override
    public String findDeserializationName(AnnotatedMethod am)
    {
        String name = _findXmlName(am);
        if (name != null) {
            return name;
        }
        return super.findDeserializationName(am);
    }
    
    @Override
    public String findDeserializationName(AnnotatedParameter ap)
    {
        String name = _findXmlName(ap);
        // empty name not acceptable...
        if (name != null && name.length() > 0) {
            return name;
        }
        return super.findDeserializationName(ap);
    }

    protected String _findXmlName(Annotated a)
    {
        JacksonXmlProperty pann = a.getAnnotation(JacksonXmlProperty.class);
        if (pann != null) {
            return pann.localName();
        }
        return null;
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

