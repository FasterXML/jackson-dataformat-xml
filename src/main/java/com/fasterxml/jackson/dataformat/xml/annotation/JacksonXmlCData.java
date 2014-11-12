package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adding this annotation will result in value of the property to be serialized
 * within a CData tag.  Only use on String properties and String collections.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlCData
{
    /**
     * Whether the property text should always be within a CData block
     * when serialized.
     */
    public boolean value() default true;
}
