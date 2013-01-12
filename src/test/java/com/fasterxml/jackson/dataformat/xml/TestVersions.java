package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class TestVersions extends XmlTestBase
{
    /**
     * Not a good to do this, but has to do, for now...
     */
    private final static int MAJOR_VERSION = 2;
    private final static int MINOR_VERSION = 1;

    // could inject using Maven filters as well...
    private final static String GROUP_ID = "com.fasterxml.jackson.dataformat";
    private final static String ARTIFACT_ID = "jackson-dataformat-xml";
    
    public void testMapperVersions()
    {
        assertVersion(new XmlMapper());
        assertVersion(new XmlFactory());
    }

    // @since 2.1
    // [Issue#48]: ObjectMapper.copy()
    public void testMapperCopy()
    {
        XmlMapper mapper1 = new XmlMapper();
        mapper1.setXMLTextElementName("foo");
        mapper1.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        
        XmlMapper mapper2 = mapper1.copy();
        assertNotSame(mapper1, mapper2);
        XmlFactory xf1 = mapper1.getFactory();
        XmlFactory xf2 = mapper2.getFactory();
        assertNotSame(xf1, xf2);
        assertEquals(XmlFactory.class, xf2.getClass());

        // and [Issue#48] as well, incomplete copy...
        assertEquals(xf1.getXMLTextElementName(), xf2.getXMLTextElementName());
        assertEquals("foo", xf2.getXMLTextElementName());
        assertEquals(xf1._xmlGeneratorFeatures, xf2._xmlGeneratorFeatures);
        assertEquals(xf1._xmlParserFeatures, xf2._xmlParserFeatures);
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    private void assertVersion(Versioned vers)
    {
        final Version v = vers.version();
        assertFalse("Should find version information (got "+v+")", v.isUknownVersion());
        assertEquals(MAJOR_VERSION, v.getMajorVersion());
        assertEquals(MINOR_VERSION, v.getMinorVersion());
        // Check patch level initially, comment out for maint versions
//        assertEquals(0, v.getPatchLevel());
        assertEquals(GROUP_ID, v.getGroupId());
        assertEquals(ARTIFACT_ID, v.getArtifactId());
    }
}

