package tools.jackson.dataformat.xml.util;

import tools.jackson.databind.JavaType;

public class TypeUtil
{
    /**
     * Helper method used for figuring out if given raw type is a collection ("indexed") type;
     * in which case a wrapper element is typically added.
     */
    public static boolean isIndexedType(JavaType type)
    {
        // 25-Mar-2024, tatu [dataformat-xml#646]: Need to support Iterable too
        if (type.isContainerType() || type.isIterationType() || type.hasRawClass(Iterable.class)) {
            // Also, should not add wrapping for Maps
            // [dataformat-xml#220]: nor map-like (Scala Map) types
            if (type.isMapLikeType()) {
                return false;
            }
            if (type.isArrayType()) {
                // Other special cases; byte[] will be serialized as base64-encoded String,
                // not real array, so...
                // (actually, ditto for char[]; thought to be a String)
                if (type.hasRawClass(byte[].class) || type.hasRawClass(char[].class)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
