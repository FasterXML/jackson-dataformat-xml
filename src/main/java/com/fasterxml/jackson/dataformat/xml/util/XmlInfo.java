package com.fasterxml.jackson.dataformat.xml.util;

/**
 * Helper container class used to contain XML specific information
 * we need to retain to construct proper bean serializer
 */
public class XmlInfo
{
    protected final String _namespace;
    protected final boolean _isAttribute;
    
    public XmlInfo(Boolean isAttribute, String ns)
    {
        _isAttribute = (isAttribute == null) ? false : isAttribute.booleanValue();
        _namespace = (ns == null) ? "" : ns;
    }

    public String getNamespace() { return _namespace; }
    public boolean isAttribute() { return _isAttribute; }
}
