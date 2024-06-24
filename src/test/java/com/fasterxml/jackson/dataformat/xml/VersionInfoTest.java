package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;

public class VersionInfoTest extends XmlTestBase
{
    public void testMapperVersions()
    {
        assertVersion(new XmlMapper());
        assertVersion(new XmlFactory());
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    private void assertVersion(Versioned vers)
    {
        final Version v = vers.version();
        assertFalse("Should find version information (got "+v+")", v.isUnknownVersion());
        Version exp = PackageVersion.VERSION;
        assertEquals(exp.toFullString(), v.toFullString());
        assertEquals(exp, v);
    }
}

