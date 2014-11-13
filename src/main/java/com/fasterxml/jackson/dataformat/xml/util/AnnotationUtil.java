package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class AnnotationUtil
{
    public static String findNamespaceAnnotation(AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                String ns = ((XmlAnnotationIntrospector) intr).findNamespace(prop);
                if (ns != null) {
                    return ns;
                }
            } else  if (intr instanceof JaxbAnnotationIntrospector) {
                String ns = ((JaxbAnnotationIntrospector) intr).findNamespace(prop);
                if (ns != null) {
                    return ns;
                }
            }
        }
        return null;
    }

    public static Boolean findIsAttributeAnnotation(AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                Boolean b = ((XmlAnnotationIntrospector) intr).isOutputAsAttribute(prop);
                if (b != null) {
                    return b;
                }
            } else  if (intr instanceof JaxbAnnotationIntrospector) {
                Boolean b = ((JaxbAnnotationIntrospector) intr).isOutputAsAttribute(prop);
                if (b != null) {
                    return b;
                }
           }
        }
        return null;
    }

    public static Boolean findIsTextAnnotation(AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                Boolean b = ((XmlAnnotationIntrospector) intr).isOutputAsText(prop);
                if (b != null) {
                    return b;
                }
            } else  if (intr instanceof JaxbAnnotationIntrospector) {
                Boolean b = ((JaxbAnnotationIntrospector) intr).isOutputAsText(prop);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    public static Boolean findIsCDataAnnotation(AnnotationIntrospector ai,
                                               AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                Boolean b = ((XmlAnnotationIntrospector) intr).isOutputAsCData(prop);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }
}
