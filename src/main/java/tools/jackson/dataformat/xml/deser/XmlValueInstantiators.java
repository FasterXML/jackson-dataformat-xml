package tools.jackson.dataformat.xml.deser;

import java.util.*;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.ValueInstantiators;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;
import tools.jackson.dataformat.xml.util.AnnotationUtil;

/**
 * {@link ValueInstantiators} implementation that renames creator properties
 * to match the names that {@link XmlBeanDeserializerModifier#updateProperties}
 * will give to the corresponding property definitions.
 *<p>
 * This is needed because {@code updateProperties()} renames property definitions
 * (e.g., {@code @JacksonXmlText} properties to {@code ""}, wrapped collections to
 * their wrapper name), but creator parameters retain their original names. Without
 * this renaming, {@code BeanDeserializerFactory.addBeanProps()} cannot link property
 * definitions to creator parameters.
 *
 * @since 3.2
 */
public class XmlValueInstantiators
    extends ValueInstantiators.Base
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected final String _cfgNameForTextValue;

    public XmlValueInstantiators(String nameForTextValue)
    {
        _cfgNameForTextValue = nameForTextValue;
    }

    static class XmlDelegatingInstantiator extends ValueInstantiator.Delegating {
        private static final long serialVersionUID = 1L;

        private final SettableBeanProperty[] _renamedCreatorProps;

        public XmlDelegatingInstantiator(ValueInstantiator delegate, SettableBeanProperty[] renamedCreatorProps) {
            super(delegate);
            this._renamedCreatorProps = renamedCreatorProps;
        }

        @Override
        public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
            return _renamedCreatorProps;
        }
    }

    @Override
    public ValueInstantiator modifyValueInstantiator(DeserializationConfig config,
            BeanDescription.Supplier beanDescRef, ValueInstantiator defaultInstantiator)
    {
        SettableBeanProperty[] creatorProps = defaultInstantiator.getFromObjectArguments(config);
        if (creatorProps == null || creatorProps.length == 0) {
            return defaultInstantiator;
        }

        // Build a map of original-property-name -> new-name for renames that
        // updateProperties() will perform on the property definitions.
        // We need to apply the same renames to creator properties so names match.
        Map<String, String> renames = _findPropertyRenames(config, beanDescRef);
        if (renames.isEmpty()) {
            // Also check for @JacksonXmlText directly on creator parameters
            // (may not appear in bean property definitions for constructor-only classes)
            if (!_hasXmlTextCreatorParam(creatorProps)) {
                return defaultInstantiator;
            }
        }

        boolean hasRenames = false;
        SettableBeanProperty[] renamedCreatorProps = Arrays.copyOf(creatorProps, creatorProps.length);
        for (int i = 0, len = renamedCreatorProps.length; i < len; ++i) {
            SettableBeanProperty prop = renamedCreatorProps[i];
            if (prop == null) {
                continue;
            }
            final String propName = prop.getName();

            // First check: direct @JacksonXmlText annotation on the creator parameter
            JacksonXmlText textAnn = prop.getAnnotation(JacksonXmlText.class);
            if (textAnn != null && textAnn.value()) {
                if (!_cfgNameForTextValue.equals(propName)) {
                    renamedCreatorProps[i] = prop.withSimpleName(_cfgNameForTextValue);
                    hasRenames = true;
                }
                continue;
            }

            // Second check: rename map from property definitions
            String newName = renames.get(propName);
            if (newName != null && !newName.equals(propName)) {
                renamedCreatorProps[i] = prop.withSimpleName(newName);
                hasRenames = true;
            }
        }

        return hasRenames ?
                new XmlDelegatingInstantiator(defaultInstantiator, renamedCreatorProps)
                : defaultInstantiator;
    }

    private boolean _hasXmlTextCreatorParam(SettableBeanProperty[] creatorProps)
    {
        for (SettableBeanProperty prop : creatorProps) {
            if (prop != null) {
                JacksonXmlText textAnn = prop.getAnnotation(JacksonXmlText.class);
                if (textAnn != null && textAnn.value()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compute the property renames that {@code updateProperties()} will apply,
     * by examining the bean's property definitions for {@code @JacksonXmlText}
     * and wrapper name annotations.
     *
     * @return Map of original-property-name to new-property-name
     */
    private Map<String, String> _findPropertyRenames(DeserializationConfig config,
            BeanDescription.Supplier beanDescRef)
    {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        Map<String, String> renames = Collections.emptyMap();

        for (BeanPropertyDefinition propDef : beanDescRef.get().findProperties()) {
            final AnnotatedMember member = propDef.getPrimaryMember();
            final String origName = propDef.getName();
            String renamed = null;

            // Check @JacksonXmlText
            Boolean isText = AnnotationUtil.findIsTextAnnotation(config, intr, member);
            if (Boolean.TRUE.equals(isText)) {
                if (!_cfgNameForTextValue.equals(origName)) {
                    renamed = _cfgNameForTextValue;
                }
            } else {
                // Check wrapper name (for Lists)
                PropertyName wrapperName = propDef.getWrapperName();
                if (wrapperName != null && wrapperName != PropertyName.NO_NAME) {
                    String localName = wrapperName.getSimpleName();
                    if (localName != null && localName.length() > 0
                            && !localName.equals(origName)) {
                        renamed = localName;
                    }
                }
            }
            if (renamed != null) {
                if (renames.isEmpty()) {
                    renames = new HashMap<>();
                }
                renames.put(origName, renamed);
            }
        }
        return renames;
    }
}
