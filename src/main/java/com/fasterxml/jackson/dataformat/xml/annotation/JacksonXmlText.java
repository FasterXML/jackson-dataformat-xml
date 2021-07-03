package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.*;

/**
 * Interface that is loosely similar to {@code jakarta.xml.bind.annotation.XmlValue}
 * in that it can be used on one (and only one!) property of a POJO.
 * It will result in value of the property be serialized without element wrapper,
 * as long as there are no element-wrapped other properties (attribute-valued
 * properties are acceptable).
 * It is also somewhat similar to core Jackson {@code @JsonValue} annotation; but
 * has to be separate as {@code @JsonValue} does not allow any other
 * properties.
 *<p>
 * Note that only one such property is allowed on a POJO: if multiple properties
 * are annotated, behavior is not defined.
 *<p>
 * Internally properties annotated will be considered to be properties with
 * no name (that is, with marker {@code ""} (empty String)).
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD,
    // @since 2.12 also allowed on (constructor) parameter
    ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlText
{
    /**
     * Whether serialization of the property should always be done as basic
     * XML text or not; if true, will be, if false, not.
     */
    public boolean value() default true;
}
