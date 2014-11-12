package com.fasterxml.jackson.dataformat.xml.jaxb;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Alternative {@link com.fasterxml.jackson.databind.AnnotationIntrospector}
 * implementation that
 * builds on {@link JaxbAnnotationIntrospector}.
 *<p>
 * NOTE: since version 2.4, it should NOT be necessary to use this class;
 * instead, plain {@link JaxbAnnotationIntrospector} should fully work.
 * With previous versions some aspects were not fully working and this
 * class was necessary.
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
    
    // Since 2.4.0, JaxbAnnotationIntrospector has implementation, so delegate
    @Override
    public String findNamespace(Annotated ann) {
        return super.findNamespace(ann);
    }

    // Since 2.4.0, JaxbAnnotationIntrospector has implementation, so delegate
    @Override
    public Boolean isOutputAsAttribute(Annotated ann) {
        return super.isOutputAsAttribute(ann);
    }
    
    // Since 2.4.0, JaxbAnnotationIntrospector has implementation, so delegate
    @Override
    public Boolean isOutputAsText(Annotated ann) {
        return super.isOutputAsText(ann);
    }

    @Override
    public Boolean isOutputAsCData(Annotated ann) {
        //There is no CData annotation in JAXB
        return false;
    }
    
    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */
 
    /*
    private String handleJaxbDefault(String value)
    {
        return MARKER_FOR_DEFAULT.equals(value) ? "" : value;
    }
    */

    @Deprecated // since 2.4; not used by this module
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
