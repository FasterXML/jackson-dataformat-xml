package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adding this annotation will result in value of the property to be serialized
 * within an xml {@code CDATA} section.  Only use on String properties and String collections.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlCData
{
    /**
     * Whether the property text should always be within a CDATA section
     * when serialized. Has no meaning for deserialization; content may come from
     * any legal character data section (CDATA or regular text segment).
     */
    public boolean value() default true;
}
