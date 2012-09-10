package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is similar to JAXB <code>javax.xml.bind.annotation.XmlElementWrapper</code>,
 * to indicate wrapper element to use (if any) for Collection types (arrays,
 * <code>java.util.Collection</code>). If defined, a separate container (wrapper) element
 * is used; if not, entries are written without wrapping.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlElementWrapper
{
    /**
     * Marker value (empty String) that denotes that the underlying property
     * name should also be used as the wrapper name, effectively "doubling"
     * start and end elements.
     * 
     * @since 2.1
     */
    public final static String USE_PROPERTY_NAME = "";
    
    String namespace() default USE_PROPERTY_NAME;
    String localName() default USE_PROPERTY_NAME;

    /**
     * Optional property that can be used to explicitly disable wrapping,
     * usually via mix-ins, or when using <code>AnnotationIntrospector</code>
     * pairs.
     * 
     * @since 2.1
     */
    boolean useWrapping() default true;
}
