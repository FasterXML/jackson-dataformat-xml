package com.fasterxml.jackson.dataformat.xml;

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

/**
 * Extension of {@link JacksonAnnotationIntrospector} that is needed to support
 * additional xml-specific annotation that Jackson provides. Note, however, that
 * there is no JAXB annotation support here; that is provided with
 * separate introspector (see
 * https://github.com/FasterXML/jackson-modules-base/tree/master/jaxb,
 * class {@code com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector}).
 */
public class JacksonXmlAnnotationIntrospector
    extends JacksonAnnotationIntrospector
    implements XmlAnnotationIntrospector
{
    private static final long serialVersionUID = 1L;

    // @since 2.11.1
    @SuppressWarnings("unchecked")
    private final static Class<? extends Annotation>[] ANNOTATIONS_TO_INFER_XML_PROP =
            (Class<? extends Annotation>[]) new Class<?>[] {
        JacksonXmlText.class, JacksonXmlElementWrapper.class
    };

    /**
     * For backwards compatibility with 2.0, the default behavior is
     * to assume use of List wrapper if no annotations are used.
     */
    public final static boolean DEFAULT_USE_WRAPPER = true;

    // non-final from 2.7 on, to allow mapper to change
    protected boolean _cfgDefaultUseWrapper;

    public JacksonXmlAnnotationIntrospector() {
        this(DEFAULT_USE_WRAPPER);
    }

    public JacksonXmlAnnotationIntrospector(boolean defaultUseWrapper) {
        _cfgDefaultUseWrapper = defaultUseWrapper;
    }

    /*
    /**********************************************************************
    /* Extended API XML format module requires
    /**********************************************************************
     */

    public void setDefaultUseWrapper(boolean b) {
        _cfgDefaultUseWrapper = b;
    }

    /*
    /**********************************************************************
    /* Overrides of JacksonAnnotationIntrospector impls
    /**********************************************************************
     */

    @Override
    public PropertyName findWrapperName(Annotated ann)
    {
        JacksonXmlElementWrapper w = _findAnnotation(ann, JacksonXmlElementWrapper.class);
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
        // 09-Sep-2012, tatu: In absence of configuration we need to use our
        //   default settings...
        if (_cfgDefaultUseWrapper) {
            return PropertyName.USE_DEFAULT;
        }
        return null;
    }
    
    @Override
    public PropertyName findRootName(AnnotatedClass ac)
    {
        JacksonXmlRootElement root = _findAnnotation(ac, JacksonXmlRootElement.class);
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
    public String findNamespace(MapperConfig<?> config, Annotated ann)
    {
        JacksonXmlProperty prop = _findAnnotation(ann, JacksonXmlProperty.class);
        if (prop != null) {
            return prop.namespace();
        }
        // 14-Nov-2020, tatu: 2.12 adds namespace for this too
        JsonProperty jprop = _findAnnotation(ann, JsonProperty.class);
        if (jprop != null) {
            return jprop.namespace();
        }
        return null;
    }

    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector, isXxx methods
    /**********************************************************************
     */

    @Override
    public Boolean isOutputAsAttribute(MapperConfig<?> config, Annotated ann)
    {
        JacksonXmlProperty prop = _findAnnotation(ann, JacksonXmlProperty.class);
        if (prop != null) {
            return prop.isAttribute() ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }
    
    @Override
    public Boolean isOutputAsText(MapperConfig<?> config, Annotated ann)
    {
        JacksonXmlText prop = _findAnnotation(ann, JacksonXmlText.class);
        if (prop != null) {
            return prop.value() ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }

    @Override
    public Boolean isOutputAsCData(MapperConfig<?> config, Annotated ann) {
        JacksonXmlCData prop = ann.getAnnotation(JacksonXmlCData.class);
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
                if (_hasOneOf(a, ANNOTATIONS_TO_INFER_XML_PROP)) {
                    return PropertyName.USE_DEFAULT;
                }
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
                if (_hasOneOf(a, ANNOTATIONS_TO_INFER_XML_PROP)) {
                    return PropertyName.USE_DEFAULT;
                }
            }
        }
        return name;
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
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
        return new XmlTypeResolverBuilder();
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected PropertyName _findXmlName(Annotated a)
    {
        JacksonXmlProperty pann = _findAnnotation(a, JacksonXmlProperty.class);
        if (pann != null) {
            return PropertyName.construct(pann.localName(), pann.namespace());
        }
        return null;
    }
}
