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

    public ElementWrapper(ElementWrapper parent) {
        _parent = parent;
        _wrapperName = null;
        _wrapperNamespace = "";
    }
    
    public ElementWrapper(ElementWrapper parent,
            String wrapperLocalName, String wrapperNamespace)
    {
        _parent = parent;
        _wrapperName = wrapperLocalName;
        _wrapperNamespace = (wrapperNamespace == null) ? "" : wrapperNamespace;
    }

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
}
