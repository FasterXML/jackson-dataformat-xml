package tools.jackson.dataformat.xml.util;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.dataformat.xml.XmlAnnotationIntrospector;

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

    // [dataformat-xml#27]
    public static String findXmlPropertyLocalName(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            if (intr instanceof XmlAnnotationIntrospector) {
                String name = ((XmlAnnotationIntrospector) intr).findXmlPropertyLocalName(config, prop);
                if (name != null) {
                    return name;
                }
            }
        }
        return null;
    }
}
