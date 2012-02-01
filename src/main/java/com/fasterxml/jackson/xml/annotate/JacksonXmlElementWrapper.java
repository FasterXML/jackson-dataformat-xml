package com.fasterxml.jackson.xml.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is similar to JAXB <code>javax.xml.bind.annotation.XmlElementWrapper</code>,
 * to indicate wrapper element to use (if any) for Collection types (arrays,
 * <code>java.util.Collection</code>). If defined, a separate container (wrapper) element
 * is used; if not, entries are written without wrapping.
 * Name of wrapper element defaults to name of the property but can be explicitly defined
 * to something else.
 * 
 * @author tatu
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlElementWrapper
{
    String namespace() default "";
    String localName() default "";
}
