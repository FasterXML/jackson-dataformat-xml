package tools.jackson.dataformat.xml;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotationIntrospectorPair;
import tools.jackson.databind.util.ClassUtil;

/**
 * Additional extension interface used above and beyond
 * {@link AnnotationIntrospector} to handle XML-specific configuration.
 */
public interface XmlAnnotationIntrospector
    extends AnnotationIntrospector.XmlExtensions
{
    /**
     * Mutant factory for getting an introspector that uses given default for
     * List wrapping (for Lists and arrays without explicit wrapper annotation):
     * returns this instance if there is no change (or no such setting), a
     * re-configured copy otherwise. Must never modify this instance, as an
     * introspector may be shared by multiple (immutable) mappers; see
     * {@code XmlMapper.rebuild()}.
     *<p>
     * Default implementation returns {@code this}, for introspectors that have
     * no such setting.
     *
     * @param defaultUseWrapper Whether to use wrapping by default or not
     *
     * @return Introspector that uses given default for wrapping
     *
     * @since 3.3
     */
    default XmlAnnotationIntrospector withDefaultUseWrapper(boolean defaultUseWrapper) {
        return this;
    }

    /*
    /**********************************************************************
    /* Replacement of 'AnnotationIntrospector.Pair' to use when combining
    /* (potential) XMLAnnotationIntrospector instance
    /**********************************************************************
     */

    /**
     * Extension of <code>AnnotationIntrospector.Pair</code> that can
     * also dispatch 'XmlAnnotationIntrospector' methods.
     */
    public static class Pair extends AnnotationIntrospectorPair
        implements XmlAnnotationIntrospector
    {
        private static final long serialVersionUID = 1L;

        protected final AnnotationIntrospector.XmlExtensions _xmlPrimary;
        protected final AnnotationIntrospector.XmlExtensions _xmlSecondary;

        public Pair(AnnotationIntrospector p, AnnotationIntrospector s)
        {
            super(p, s);
            if (p instanceof AnnotationIntrospector.XmlExtensions xmlExt) {
                _xmlPrimary = xmlExt;
            } else {
                _xmlPrimary = null;
            }

            if (s instanceof AnnotationIntrospector.XmlExtensions xmlExt) {
                _xmlSecondary = xmlExt;
            } else {
                _xmlSecondary = null;
            }
        }

        public static XmlAnnotationIntrospector.Pair instance(AnnotationIntrospector a1, AnnotationIntrospector a2) {
            return new XmlAnnotationIntrospector.Pair(a1, a2);
        }

        /**
         * Sub-classes MUST override this method to retain their type; otherwise
         * an {@link IllegalStateException} is thrown when a re-configured copy
         * would be needed.
         *
         * @since 3.3
         */
        @Override
        public XmlAnnotationIntrospector withDefaultUseWrapper(boolean defaultUseWrapper)
        {
            AnnotationIntrospector p = _withDefaultUseWrapper(_primary, defaultUseWrapper);
            AnnotationIntrospector s = _withDefaultUseWrapper(_secondary, defaultUseWrapper);
            if ((p == _primary) && (s == _secondary)) {
                return this;
            }
            ClassUtil.verifyMustOverride(XmlAnnotationIntrospector.Pair.class, this,
                    "withDefaultUseWrapper");
            return new XmlAnnotationIntrospector.Pair(p, s);
        }

        protected static AnnotationIntrospector _withDefaultUseWrapper(AnnotationIntrospector ai,
                boolean defaultUseWrapper)
        {
            if (ai instanceof XmlAnnotationIntrospector xmlAi) {
                return (AnnotationIntrospector) xmlAi.withDefaultUseWrapper(defaultUseWrapper);
            }
            return ai;
        }
        
        @Override
        public String findNamespace(MapperConfig<?> config, Annotated ann)
        {
            String value = (_xmlPrimary == null) ? null : _xmlPrimary.findNamespace(config, ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.findNamespace(config, ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsAttribute(MapperConfig<?> config, Annotated ann)
        {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsAttribute(config, ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.isOutputAsAttribute(config, ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsText(MapperConfig<?> config, Annotated ann)
        {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsText(config, ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.isOutputAsText(config, ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsCData(MapperConfig<?> config, Annotated ann) {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsCData(config, ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.isOutputAsCData(config, ann);
            }
            return value;
        }

        // [dataformat-xml#27]
        @Override
        public PropertyName findXmlPropertyInnerName(MapperConfig<?> config, Annotated ann)
        {
            PropertyName value = (_xmlPrimary == null) ? null
                    : _xmlPrimary.findXmlPropertyInnerName(config, ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.findXmlPropertyInnerName(config, ann);
            }
            return value;
        }
    }
}
