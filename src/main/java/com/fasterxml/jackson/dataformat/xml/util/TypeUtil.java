package com.fasterxml.jackson.dataformat.xml.util;

import java.util.Map;

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
            if (cls == byte[].class || cls == byte[].class) {
                return false;
            }
            // issue#5: also, should not add wrapping for Maps
            if (Map.class.isAssignableFrom(cls)) {
                return false;
            }
            return true;
        }
        return false;
    }    

}
