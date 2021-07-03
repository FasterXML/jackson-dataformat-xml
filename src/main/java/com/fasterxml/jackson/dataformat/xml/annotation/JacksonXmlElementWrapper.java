package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is similar to JAXB {@code jakarta.xml.bind.annotation.XmlElementWrapper},
 * to indicate wrapper element to use (if any) for Collection types (arrays,
 * {@link java.util.Collection}). If defined, a separate container (wrapper) element
 * is used; if not, entries are written without wrapping.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD,
    // @since 2.12 also allowed on (constructor) parameter
    ElementType.PARAMETER})
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
