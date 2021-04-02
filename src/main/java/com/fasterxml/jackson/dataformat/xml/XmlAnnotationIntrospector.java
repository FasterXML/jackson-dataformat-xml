package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;

/**
 * Additional extension interface used above and beyond
 * {@link AnnotationIntrospector} to handle XML-specific configuration.
 */
public interface XmlAnnotationIntrospector
    extends AnnotationIntrospector.XmlExtensions
{
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
            if (p instanceof AnnotationIntrospector.XmlExtensions) {
                _xmlPrimary = (AnnotationIntrospector.XmlExtensions) p;
            } else {
                _xmlPrimary = null;
            }

            if (s instanceof AnnotationIntrospector.XmlExtensions) {
                _xmlSecondary = (AnnotationIntrospector.XmlExtensions) s;
            } else {
                _xmlSecondary = null;
            }
        }

        public static XmlAnnotationIntrospector.Pair instance(AnnotationIntrospector a1, AnnotationIntrospector a2) {
            return new XmlAnnotationIntrospector.Pair(a1, a2);
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
    }
}
