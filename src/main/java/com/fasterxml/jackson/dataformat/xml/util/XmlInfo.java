package com.fasterxml.jackson.dataformat.xml.util;

/**
 * Helper container class used to contain XML specific information
 * we need to retain to construct proper bean serializer
 */
public class XmlInfo
{
    protected final String _namespace;
    protected final boolean _isAttribute;
    protected final boolean _isText;
    
    public XmlInfo(Boolean isAttribute, String ns, Boolean isText)
    {
        _isAttribute = (isAttribute == null) ? false : isAttribute.booleanValue();
        _namespace = (ns == null) ? "" : ns;
        _isText = (isText == null) ? false : isText.booleanValue();
    }

    public String getNamespace() { return _namespace; }
    public boolean isAttribute() { return _isAttribute; }
    public boolean isText() { return _isText; }
}
