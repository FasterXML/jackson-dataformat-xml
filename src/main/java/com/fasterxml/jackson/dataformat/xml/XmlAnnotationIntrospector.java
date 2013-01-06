package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;

/**
 * Additional extension interface used above and beyond
 * {@link AnnotationIntrospector} to handle XML-specific configuration.
 */
public interface XmlAnnotationIntrospector
{
    /**
     * Method that can be called to figure out generic namespace
     * property for an annotated object.
     *
     * @return Null if annotated thing does not define any
     *   namespace information; non-null namespace (which may
     *   be empty String) otherwise
     */
    public String findNamespace(Annotated ann);

    // Method used to check if specified property has annotation that indicates
    // that it should be wrapped in an element; and if so, name to use.
    // Note: local name of "" is used to indicate that name should default
    // to using name (local name and namespace) of property itself.
    //
    // Removed in 2.1
//    public QName findWrapperElement(Annotated ann);

    // Method used to find out name to use for the outermost (root) XML element
    // name when serializing (since there is no property that would define it);
    // this overrides default name based on type of object.
    //
    // Removed in 2.1
//    public QName findRootElement(Annotated ann);

    /**
     * Method used to check whether given annotated element
     * (field, method, constructor parameter) has indicator that suggests
     * it be output as an XML attribute or not (as element)
     */
    public Boolean isOutputAsAttribute(Annotated ann);

    /**
     * Method used to check whether given annotated element
     * (field, method, constructor parameter) has indicator that suggests
     * it should be serialized as text, without element wrapper.
     */
    public Boolean isOutputAsText(Annotated ann);
    
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
        
        protected final XmlAnnotationIntrospector _xmlPrimary;
        protected final XmlAnnotationIntrospector _xmlSecondary;
        
        public Pair(AnnotationIntrospector p, AnnotationIntrospector s)
        {
            super(p, s);
            _xmlPrimary = (p instanceof XmlAnnotationIntrospector) ? (XmlAnnotationIntrospector) p : null;
            _xmlSecondary = (s instanceof XmlAnnotationIntrospector) ? (XmlAnnotationIntrospector) s : null;
        }

        public static XmlAnnotationIntrospector.Pair instance(AnnotationIntrospector a1, AnnotationIntrospector a2) {
            return new XmlAnnotationIntrospector.Pair(a1, a2);
        }
        
        @Override
        public String findNamespace(Annotated ann)
        {
            String value = (_xmlPrimary == null) ? null : _xmlPrimary.findNamespace(ann);
            if (value == null && _xmlSecondary != null) {
                value = _xmlSecondary.findNamespace(ann);
            }
            return value;
        }

        // These were removed in 2.1
        /*
        @Override
        public QName findWrapperElement(Annotated ann)
        {
            QName value = (_xmlPrimary == null) ? null : _xmlPrimary.findWrapperElement(ann);
            if (value == null && _xmlSecondary != null) {
                value = _xmlSecondary.findWrapperElement(ann);
            }
            return value;
        }

        @Override
        public QName findRootElement(Annotated ann)
        {
            QName value = (_xmlPrimary == null) ? null : _xmlPrimary.findRootElement(ann);
            if (value == null && _xmlSecondary != null) {
                value = _xmlSecondary.findRootElement(ann);
            }
            return value;
        }
        */
        
        @Override
        public Boolean isOutputAsAttribute(Annotated ann)
        {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsAttribute(ann);
            if (value == null && _xmlSecondary != null) {
                value = _xmlSecondary.isOutputAsAttribute(ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsText(Annotated ann)
        {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsText(ann);
            if (value == null && _xmlSecondary != null) {
                value = _xmlSecondary.isOutputAsText(ann);
            }
            return value;
        }
    
    }
}
