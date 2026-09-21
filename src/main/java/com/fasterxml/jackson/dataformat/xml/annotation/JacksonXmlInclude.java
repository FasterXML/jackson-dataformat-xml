package com.fasterxml.jackson.dataformat.xml.annotation;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
/**
 * Annotation that can be used to provide XML-specific configuration
 * for include properties like {@code @JsonInclude} for json.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD, 
         ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlInclude {
    Include value() default Include.ALWAYS;
    Include content() default Include.ALWAYS;
    Class<?> valueFilter() default Void.class;
    Class<?> contentFilter() default Void.class;
    
    public enum Include {
        ALWAYS,
        NON_NULL,
        NON_ABSENT,
        NON_EMPTY,
        NON_DEFAULT,
        CUSTOM
    }
}