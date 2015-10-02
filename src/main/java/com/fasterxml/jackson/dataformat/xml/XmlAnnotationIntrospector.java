package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

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

    /**
     * Method used to check whether given annotated element
     * (field, method, constructor parameter) has indicator that suggests
     * it should be wrapped in a CDATA tag.
     */
    public Boolean isOutputAsCData(Annotated ann);

    /**
     * @since 2.7
     */
    public void setDefaultUseWrapper(boolean b);
    
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
            if (p instanceof XmlAnnotationIntrospector) {
                _xmlPrimary = (XmlAnnotationIntrospector) p;
            } else if (p instanceof JaxbAnnotationIntrospector) {
                _xmlPrimary = new JaxbWrapper((JaxbAnnotationIntrospector) p);
            } else {
                _xmlPrimary = null;
            }

            if (s instanceof XmlAnnotationIntrospector) {
                _xmlSecondary = (XmlAnnotationIntrospector) s;
            } else if (s instanceof JaxbAnnotationIntrospector) {
                _xmlSecondary = new JaxbWrapper((JaxbAnnotationIntrospector) s);
            } else {
                _xmlSecondary = null;
            }
        }

        public static XmlAnnotationIntrospector.Pair instance(AnnotationIntrospector a1, AnnotationIntrospector a2) {
            return new XmlAnnotationIntrospector.Pair(a1, a2);
        }
        
        @Override
        public String findNamespace(Annotated ann)
        {
            String value = (_xmlPrimary == null) ? null : _xmlPrimary.findNamespace(ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.findNamespace(ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsAttribute(Annotated ann)
        {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsAttribute(ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.isOutputAsAttribute(ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsText(Annotated ann)
        {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsText(ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.isOutputAsText(ann);
            }
            return value;
        }

        @Override
        public Boolean isOutputAsCData(Annotated ann) {
            Boolean value = (_xmlPrimary == null) ? null : _xmlPrimary.isOutputAsCData(ann);
            if ((value == null) && (_xmlSecondary != null)) {
                value = _xmlSecondary.isOutputAsCData(ann);
            }
            return value;
        }

        @Override
        public void setDefaultUseWrapper(boolean b) {
            if (_xmlPrimary != null) {
                _xmlPrimary.setDefaultUseWrapper(b);
            }
            if (_xmlSecondary != null) {
                _xmlSecondary.setDefaultUseWrapper(b);
            }
        }
    }

    /*
    /**********************************************************************
    /* Helper class used to adapt JaxbAnnoationIntrospector as
    /* XmlAnnotationIntrospector
    /**********************************************************************
     */

    /**
     * Wrapper we need to adapt {@link JaxbAnnotationIntrospector} as
     * {@link XmlAnnotationIntrospector}: something we can not (alas!)
     * do in JAXB module because of dependency direction (JAXB module
     * has no knowledge of this module).
     */
    static class JaxbWrapper implements XmlAnnotationIntrospector
    {
        protected final JaxbAnnotationIntrospector _intr;

        public JaxbWrapper(JaxbAnnotationIntrospector i) {
            _intr = i;
        }
        
        @Override
        public String findNamespace(Annotated ann) {
            return _intr.findNamespace(ann);
        }

        @Override
        public Boolean isOutputAsAttribute(Annotated ann) {
            return _intr.isOutputAsAttribute(ann);
        }

        @Override
        public Boolean isOutputAsText(Annotated ann) {
            return _intr.isOutputAsText(ann);
        }

        @Override
        public Boolean isOutputAsCData(Annotated ann) {
            //There is no CData annotation in JAXB
            return null;
        }

        @Override
        public void setDefaultUseWrapper(boolean b) {
            // not used with JAXB
        }
    }
}
