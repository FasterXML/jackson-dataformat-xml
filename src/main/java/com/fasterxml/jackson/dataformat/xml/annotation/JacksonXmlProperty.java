package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to provide XML-specific configuration
 * for properties, above and beyond what
 * {@link com.fasterxml.jackson.annotation.JsonProperty} contains.
 * It is an alternative to using JAXB annotations.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlProperty
{
    boolean isAttribute() default false;
    String namespace() default "";
    String localName() default "";
}
