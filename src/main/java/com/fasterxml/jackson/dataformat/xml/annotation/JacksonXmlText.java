package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.*;

/**
 * Interface that is loosely similar to {@link javax.xml.bind.annotation.XmlValue}
 * in that it can be used on one (and only one!) property of a POJO.
 * It will result in value of the property be serialized without element wrapper,
 * as long as there are no element-wrapped other properties (attribute-valued
 * properties are acceptable).
 * It is also similar to core Jackson <code>JsonValue</code> annotation; but
 * has to be separate as <code>JsonValue</code> does not allow any other
 * properties.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JacksonXmlText
{
    /**
     * Whether serialization of the property should always be done as basic
     * XML text or not; if true, will be, if false, not.
     */
    public boolean value() default true;
}
