package tools.jackson.dataformat.xml.util;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;

/**
 * Helper class for accessing XML-specific annotation information via
 * {@link AnnotationIntrospector.XmlExtensions} introspectors.
 *<p>
 * NOTE: all methods accept {@code null} member and return {@code null} in that
 * case: property definitions without any accessors do occur (see
 * [dataformat-xml#884]) and cannot have annotations to find, anyway.
 */
public class AnnotationUtil
{
    public static String findNamespaceAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        if (prop != null) {
            for (AnnotationIntrospector intr : ai.allIntrospectors()) {
                if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                    String ns = ((AnnotationIntrospector.XmlExtensions) intr).findNamespace(config, prop);
                    if (ns != null) {
                        return ns;
                    }
                }
            }
        }
        return null;
    }

    public static Boolean findIsAttributeAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        if (prop != null) {
            for (AnnotationIntrospector intr : ai.allIntrospectors()) {
                if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                    Boolean b = ((AnnotationIntrospector.XmlExtensions) intr).isOutputAsAttribute(config, prop);
                    if (b != null) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    public static Boolean findIsTextAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        if (prop != null) {
            for (AnnotationIntrospector intr : ai.allIntrospectors()) {
                if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                    Boolean b = ((AnnotationIntrospector.XmlExtensions) intr).isOutputAsText(config, prop);
                    if (b != null) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    public static Boolean findIsCDataAnnotation(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        if (prop != null) {
            for (AnnotationIntrospector intr : ai.allIntrospectors()) {
                if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                    Boolean b = ((AnnotationIntrospector.XmlExtensions) intr).isOutputAsCData(config, prop);
                    if (b != null) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    // For [dataformat-xml#27]
    /**
     * @since 3.2
     */
    public static PropertyName findXmlPropertyInnerName(MapperConfig<?> config,
            AnnotationIntrospector ai,
            AnnotatedMember prop)
    {
        if (prop != null) {
            for (AnnotationIntrospector intr : ai.allIntrospectors()) {
                if (intr instanceof AnnotationIntrospector.XmlExtensions) {
                    PropertyName name = ((AnnotationIntrospector.XmlExtensions) intr).findXmlPropertyInnerName(config, prop);
                    if (name != null) {
                        return name;
                    }
                }
            }
        }
        return null;
    }
}
