package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

public class AnnotationUtil
{
    public static String findNamespaceAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                String ns = ((AnnotationIntrospector.XmlExtensions) intr).findNamespace(config, prop);
                if (ns != null) {
                    return ns;
                }
            }
        }
        return null;
    }

    public static Boolean findIsAttributeAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                Boolean b = ((AnnotationIntrospector.XmlExtensions) intr).isOutputAsAttribute(config, prop);
                if (b != null) {
                    return b;
                }
           }
        }
        return null;
    }

    public static Boolean findIsTextAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                Boolean b = ((AnnotationIntrospector.XmlExtensions) intr).isOutputAsText(config, prop);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    public static Boolean findIsCDataAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                Boolean b = ((AnnotationIntrospector.XmlExtensions) intr).isOutputAsCData(config, prop);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }
}
