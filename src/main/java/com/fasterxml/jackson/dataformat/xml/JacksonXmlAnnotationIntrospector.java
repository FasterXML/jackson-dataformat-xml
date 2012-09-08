package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.PropertyName;
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
    /* Overrides of JacksonAnnotationIntrospector impls
    /**********************************************************************
     */

    @Override
    public PropertyName findWrapperName(Annotated ann)
    {
        JacksonXmlElementWrapper w = ann.getAnnotation(JacksonXmlElementWrapper.class);
        if (w != null) {
            return PropertyName.construct(w.localName(), w.namespace());
        }
        return null;
    }
    
    @Override
    public PropertyName findRootName(AnnotatedClass ac)
    {
        JacksonXmlRootElement root = ac.getAnnotation(JacksonXmlRootElement.class);
        if (root != null) {
            String local = root.localName();
            String ns = root.namespace();
            
            if (local.length() == 0 && ns.length() == 0) {
                return PropertyName.USE_DEFAULT;
            }
            return new PropertyName(local, ns);
        }
        return super.findRootName(ac);
    }
    
    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector, findXxx
    /**********************************************************************
     */

    @Override
    public String findNamespace(Annotated ann)
    {
        JacksonXmlProperty prop = ann.getAnnotation(JacksonXmlProperty.class);
        if (prop != null) {
            return prop.namespace();
        }
        return null;
    }

    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector, isXxx methods
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
    public Boolean isOutputAsText(Annotated ann)
    {
    	JacksonXmlText prop = ann.getAnnotation(JacksonXmlText.class);
        if (prop != null) {
            return prop.value() ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }
    
    /*
    /**********************************************************************
    /* Overrides for name, property detection
    /**********************************************************************
     */

    @Override
    public PropertyName findNameForSerialization(Annotated a)
    {
        PropertyName name = _findXmlName(a);
        return (name == null) ? super.findNameForSerialization(a) : name;
    }

    @Deprecated
    @Override
    public String findSerializationName(AnnotatedField af)
    {
        PropertyName name = _findXmlName(af);
        if (name != null) {
            return name.getSimpleName();
        }
        return super.findSerializationName(af);
    }

    @Deprecated
    @Override
    public String findSerializationName(AnnotatedMethod am)
    {
        PropertyName name = _findXmlName(am);
        if (name != null) {
            return name.getSimpleName();
        }
        return super.findSerializationName(am);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = _findXmlName(a);
        return (name == null) ? super.findNameForDeserialization(a) : name;
    }
    
    @Deprecated
    @Override
    public String findDeserializationName(AnnotatedField af)
    {
        PropertyName name = _findXmlName(af);
        if (name != null) {
            return name.getSimpleName();
        }
        return super.findDeserializationName(af);
    }

    @Deprecated
    @Override
    public String findDeserializationName(AnnotatedMethod am)
    {
        PropertyName name = _findXmlName(am);
        if (name != null) {
            return name.getSimpleName();
        }
        return super.findDeserializationName(am);
    }
    
    @Deprecated
    @Override
    public String findDeserializationName(AnnotatedParameter ap)
    {
        PropertyName name = _findXmlName(ap);
        if (name != null) {
            // empty name not acceptable...
            String local = name.getSimpleName();
            if (local != null && local.length() > 0) {
                return local;
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

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected PropertyName _findXmlName(Annotated a)
    {
        JacksonXmlProperty pann = a.getAnnotation(JacksonXmlProperty.class);
        if (pann != null) {
            return PropertyName.construct(pann.localName(), pann.namespace());
        }
        return null;
    }
}

