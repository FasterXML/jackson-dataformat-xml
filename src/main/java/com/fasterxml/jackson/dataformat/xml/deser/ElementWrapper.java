package com.fasterxml.jackson.dataformat.xml.deser;

/**
 * Helper class needed to keep track of virtual wrapper elements
 * added in the logical XML token stream.
 */
class ElementWrapper
{
    protected final ElementWrapper _parent;
    protected final String _wrapperName;
    protected final String _wrapperNamespace;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    private ElementWrapper(ElementWrapper parent) {
        _parent = parent;
        _wrapperName = null;
        _wrapperNamespace = "";
    }
    
    private ElementWrapper(ElementWrapper parent,
            String wrapperLocalName, String wrapperNamespace)
    {
        _parent = parent;
        _wrapperName = wrapperLocalName;
        _wrapperNamespace = (wrapperNamespace == null) ? "" : wrapperNamespace;
    }

    /**
     * Factory method called to construct a new "matching" wrapper element,
     * at level where virtual wrapping is needed.
     */
    public static ElementWrapper matchingWrapper(ElementWrapper parent,
            String wrapperLocalName, String wrapperNamespace)
    {
        return new ElementWrapper(parent, wrapperLocalName, wrapperNamespace);
    }

    /**
     * Factory method used for creating intermediate wrapper level, which
     * is only used for purpose of keeping track of physical element
     * nesting.
     */
    public ElementWrapper intermediateWrapper() {
        return new ElementWrapper(this, null, null);
    }
    
    /*
    /**********************************************************
    /* API
    /**********************************************************
     */

    public boolean isMatching() { return _wrapperName != null; }
    
    public String getWrapperLocalName() { return _wrapperName; }
    public String getWrapperNamespace() { return _wrapperNamespace; }

    public ElementWrapper getParent() { return _parent; }

    public boolean matchesWrapper(String localName, String ns)
    {
        // null means "anything goes", so:
        if (_wrapperName == null) {
            return true;
        }
        if (ns == null) {
            ns = "";
        }
        return _wrapperName.equals(localName) && _wrapperNamespace.equals(ns);
    }

    /*
    /**********************************************************
    /* Overrides
    /**********************************************************
     */

    @Override
    public String toString()
    {
        if (_parent == null) {
            return "Wrapper: ROOT, matching: "+_wrapperName;
        }
        if (_wrapperName == null) {
            return "Wrapper: empty";
        }
        return "Wrapper: branch, matching: "+_wrapperName;
    }
}
