package com.fasterxml.jackson.dataformat.xml.util;

import java.util.Collection;

import com.fasterxml.jackson.databind.JavaType;

public class TypeUtil
{
    /**
     * Helper method used for figuring out if given raw type is a collection ("indexed") type;
     * in which case a wrapper element is typically added.
     */
    public static boolean isIndexedType(JavaType type)
    {
        if (type.isContainerType()) {
            Class<?> cls = type.getRawClass();
            // One special case; byte[] will be serialized as base64-encoded String, not real array, so:
            // (actually, ditto for char[]; thought to be a String)
            if (cls == byte[].class || cls == char[].class) {
                return false;
            }
            // Also, should not add wrapping for Maps
            // [dataformat-xml#220]: nor map-like (Scala Map) types
            if (type.isMapLikeType()) {
                return false;
            }
            return true;
        }
        return false;
    }    

    public static boolean isIndexedType(Class<?> cls)
    {
        return (cls.isArray() && cls != byte[].class && cls != char[].class)
                || Collection.class.isAssignableFrom(cls);
    }
}
