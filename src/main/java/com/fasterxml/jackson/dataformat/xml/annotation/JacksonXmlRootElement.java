package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define name of root element used
 * for the root-level object when serialized, which normally uses
 * name of the type (class). It is similar to JAXB <code>XmlRootElement</code>.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlRootElement
{
    public static String DEFAULT_WRAPPER_NAME="item";

    String namespace() default "";
    String localName() default "";
    String wrapperForIndexedType() default DEFAULT_WRAPPER_NAME;
}
