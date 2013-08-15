package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Test case(s) to see that we can map "empty" content into null, for convenience.
 * Currently Stax parser will throw an exception; we could either use buffering,
 * or catch the EOFException and take that as a hint.
 */
public class TestEmptyContent extends XmlTestBase
{
    static class EmptyBean { }

    /**
     * [Issue#60]: should be able to detect "no content", ideally.
     */
    public void testNoContent() throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        assertNull(mapper.readValue("", EmptyBean.class));
    }
}
