package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.databind.JavaType;

public class TypeUtil
{
    /**
     * Helper method used for figuring out if given raw type is a collection ("indexed") type;
     * in which case a wrapper element is typically added.
     */
    public static boolean isIndexedType(JavaType type)
    {
        Class<?> cls = type.getRawClass();
        if (type.isContainerType() || type.isIterationType()) {
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
}
