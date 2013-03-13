package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Alternative {@link com.fasterxml.jackson.databind.AnnotationIntrospector}
 * implementation that
 * builds on introspector from Jackson XC package that uses JAXB annotations,
 * not Jackson annotations.
 */
public class XmlJaxbAnnotationIntrospector
    extends JaxbAnnotationIntrospector
    implements XmlAnnotationIntrospector
{
	private static final long serialVersionUID = 6477843393758275877L;

	@Deprecated
    public XmlJaxbAnnotationIntrospector() {
        super();
    }

    public XmlJaxbAnnotationIntrospector(TypeFactory typeFactory) {
        super(typeFactory);
    }
    
    /*
    /**********************************************************************
    /* XmlAnnotationIntrospector overrides
    /**********************************************************************
     */
    
    @Override
    public String findNamespace(Annotated ann)
    {
        String ns = null;

        /* 10-Oct-2009, tatus: I suspect following won't work quite
         *  as well as it should, wrt. defaulting to package.
         *  But it should work well enough to get things started --
         *  currently this method is not needed, and when it is,
         *  this can be improved.
         */
        if (ann instanceof AnnotatedClass) {
            /* For classes, it must be @XmlRootElement. Also, we do
             * want to use defaults from package, base class
             */
            XmlRootElement elem = findRootElementAnnotation((AnnotatedClass) ann);
            if (elem != null) {
                ns = elem.namespace();
            }
        } else {
            // For others, XmlElement or XmlAttribute work (anything else?)
            XmlElement elem = findAnnotation(XmlElement.class, ann, false, false, false);
            if (elem != null) {
                ns = elem.namespace();
            }
            if (ns == null || MARKER_FOR_DEFAULT.equals(ns)) {
                XmlAttribute attr = findAnnotation(XmlAttribute.class, ann, false, false, false);
                if (attr != null) {
                    ns = attr.namespace();
                }
            }
        }
        // JAXB uses marker for "not defined"
        if (MARKER_FOR_DEFAULT.equals(ns)) {
            ns = null;
        }
        return ns;
    }

    /**
     * Here we assume fairly simple logic; if there is <code>XmlAttribute</code> to be found,
     * we consider it an attibute; if <code>XmlElement</code>, not-an-attribute; and otherwise
     * we will consider there to be no information.
     * Caller is likely to default to considering things as elements.
     */
    @Override
    public Boolean isOutputAsAttribute(Annotated ann)
    {
        XmlAttribute attr = findAnnotation(XmlAttribute.class, ann, false, false, false);
        if (attr != null) {
            return Boolean.TRUE;
        }
        XmlElement elem = findAnnotation(XmlElement.class, ann, false, false, false);
        if (elem != null) {
            return Boolean.FALSE;
        }
        return null;
    }
    
    @Override
    public Boolean isOutputAsText(Annotated ann)
    {
    	XmlValue attr = findAnnotation(XmlValue.class, ann, false, false, false);
        if (attr != null) {
            return Boolean.TRUE;
        }
        return null;
    }
    
    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */
    
    private XmlRootElement findRootElementAnnotation(AnnotatedClass ac)
    {
        // Yes, check package, no class (already included), yes superclasses
        return findAnnotation(XmlRootElement.class, ac, true, false, true);
    }

    /*
    private String handleJaxbDefault(String value)
    {
        return MARKER_FOR_DEFAULT.equals(value) ? "" : value;
    }
    */

    /* NOTE: copied verbatim from Jackson 1.9, since its visibility was
     * lowered (accidentally...)
     */
    protected <A extends Annotation> A findAnnotation(Class<A> annotationClass, Annotated annotated,
            boolean includePackage, boolean includeClass, boolean includeSuperclasses)
    {
        A annotation = annotated.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Class<?> memberClass = null;
        if (annotated instanceof AnnotatedParameter) {
            memberClass = ((AnnotatedParameter) annotated).getDeclaringClass();
        } else {
            AnnotatedElement annType = annotated.getAnnotated();
            if (annType instanceof Member) {
                memberClass = ((Member) annType).getDeclaringClass();
                if (includeClass) {
                    annotation = (A) memberClass.getAnnotation(annotationClass);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            } else if (annType instanceof Class<?>) {
                memberClass = (Class<?>) annType;
            } else {
                throw new IllegalStateException("Unsupported annotated member: " + annotated.getClass().getName());
            }
        }
        if (memberClass != null) {
            if (includeSuperclasses) {
                Class<?> superclass = memberClass.getSuperclass();
                while (superclass != null && superclass != Object.class) {
                    annotation = (A) superclass.getAnnotation(annotationClass);
                    if (annotation != null) {
                        return annotation;
                    }
                    superclass = superclass.getSuperclass();
                }
            }
            if (includePackage) {
                Package pkg = memberClass.getPackage();
                if (pkg != null) {
                    return memberClass.getPackage().getAnnotation(annotationClass);
                }
            }
        }
        return null;
    }
}
