package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to provide XML-specific configuration
 * for properties, above and beyond what
 * {@link com.fasterxml.jackson.annotation.JsonProperty} contains.
 * It is mainly an alternative to using JAXB annotations.
 *<p>
 * Note that annotation may be used on
 *<ul>
 * <li>Fields
 *  </li>
 * <li>Setter and getter methods
 *  </li>
 * <li>Arguments (parameters) of "Creators" -- annotated constructors and/or
 *    static factory methods
 *  </li>
 * </ul>
 * but it can NOT be used on argument/parameter of setter methods (or rather
 * while compiler allows that, will have no effect) -- setter method itself
 * needs to be annotated.
 *<p>
 * Note that since 2.12 there is no need to use this property over
 * {@link com.fasterxml.jackson.annotation.JsonProperty} just to define XML namespace,
 * as {@code @JsonProperty} has {@code namespace} property.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlProperty
{
    boolean isAttribute() default false;
    String namespace() default "";
    String localName() default "";
}
