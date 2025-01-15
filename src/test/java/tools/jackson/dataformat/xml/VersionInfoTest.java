package tools.jackson.dataformat.xml;

import org.junit.jupiter.api.Test;

import tools.jackson.core.Version;
import tools.jackson.core.Versioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class VersionInfoTest extends XmlTestUtil
{
    @Test
    public void testMapperVersions()
    {
        assertVersion(new XmlMapper());
        assertVersion(new XmlFactory());
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */
    
    private void assertVersion(Versioned vers)
    {
        final Version v = vers.version();
        assertFalse(v.isUnknownVersion(), "Should find version information (got "+v+")");
        Version exp = PackageVersion.VERSION;
        assertEquals(exp.toFullString(), v.toFullString());
        assertEquals(exp, v);
    }
}

