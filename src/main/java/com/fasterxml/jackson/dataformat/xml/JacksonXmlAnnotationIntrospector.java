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
    private static final long serialVersionUID = 1L;

    /**
     * For backwards compatibility with 2.0, the default behavior is
     * to assume use of List wrapper if no annotations are used.
     */
    public final static boolean DEFAULT_USE_WRAPPER = true;

    protected final boolean _cfgDefaultUseWrapper;
    
    public JacksonXmlAnnotationIntrospector() {
        this(DEFAULT_USE_WRAPPER);
    }

    public JacksonXmlAnnotationIntrospector(boolean defaultUseWrapper) {
        _cfgDefaultUseWrapper = defaultUseWrapper;
    }

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
            // Special case: wrapping explicitly blocked?
            if (!w.useWrapping()) {
                return PropertyName.NO_NAME;
            }
            // also: need to ensure we use marker:
            String localName = w.localName();
            if (localName == null || localName.length() == 0) {
                return PropertyName.USE_DEFAULT;
            }
            return PropertyName.construct(w.localName(), w.namespace());
        }
        /* 09-Sep-2012, tatu: In absence of configurating we need to use our
         *   default settings...
         */
        if (_cfgDefaultUseWrapper) {
            return PropertyName.USE_DEFAULT;
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
        if (name == null) {
            name = super.findNameForSerialization(a);
            if (name == null) {
                if (a.hasAnnotation(JacksonXmlText.class)) {
                    return PropertyName.USE_DEFAULT;
                }
            }
        }
        return name;
    }

    /* Ok, now: following contains quite a bit of duplication.
     * But it is still needed since trying to refactor things tends
     * to end in infinite loop or stack overflow... we can eventually
     * remove these methods once we are certain that core databinding
     * never calls these methods (perhaps with 2.3 or at latest 2.4)
     */
    
    @Deprecated
    @Override
    public String findSerializationName(AnnotatedField a)
    {
        PropertyName pname = _findXmlName(a);
        if (pname != null) {
            return pname.getSimpleName();
        }
        String name = super.findSerializationName(a);
        if (name == null) {
            if (a.hasAnnotation(JacksonXmlText.class)) {
                return "";
            }
        }
        return name;
    }

    @Deprecated
    @Override
    public String findSerializationName(AnnotatedMethod a)
    {
        PropertyName pname = _findXmlName(a);
        if (pname != null) {
            return pname.getSimpleName();
        }
        String name = super.findSerializationName(a);
        if (name == null) {
            if (a.hasAnnotation(JacksonXmlText.class)) {
                return "";
            }
        }
        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = _findXmlName(a);
        if (name == null) {
            name = super.findNameForDeserialization(a);
            if (name == null) {
                if (a.hasAnnotation(JacksonXmlText.class)) {
                    return PropertyName.USE_DEFAULT;
                }
            }
        }
        return name;
    }
    
    @Deprecated
    @Override
    public String findDeserializationName(AnnotatedField a)
    {
        PropertyName pname = _findXmlName(a);
        if (pname != null) {
            return pname.getSimpleName();
        }
        String name = super.findDeserializationName(a);
        if (name == null) {
            if (a.hasAnnotation(JacksonXmlText.class)) {
                return "";
            }
        }
        return name;
    }

    @Deprecated
    @Override
    public String findDeserializationName(AnnotatedMethod a)
    {
        PropertyName pname = _findXmlName(a);
        if (pname != null) {
            return pname.getSimpleName();
        }
        String name = super.findDeserializationName(a);
        if (name == null) {
            if (a.hasAnnotation(JacksonXmlText.class)) {
                return "";
            }
        }
        return name;
    }
    
    @Deprecated
    @Override
    public String findDeserializationName(AnnotatedParameter a)
    {
        PropertyName pname = _findXmlName(a);
        if (pname != null) {
            // empty name not acceptable...
            String local = pname.getSimpleName();
            if (local != null && local.length() > 0) {
                return local;
            }
        }
        // xml text still has special handling...
        String name = super.findDeserializationName(a);
        if (name == null) {
            if (a.hasAnnotation(JacksonXmlText.class)) {
                return "";
            }
        }
        return super.findDeserializationName(a);
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
