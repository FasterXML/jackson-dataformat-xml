package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class ListDeser319Test extends XmlTestBase
{
    static class Value319 {
        public Long orderId, orderTypeId;
    }    

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testEmptyList319() throws Exception
    {
        final String DOC = "<orders></orders>";
        List<Value319> value = MAPPER.readValue(DOC,
                new TypeReference<List<Value319>>() { });
        assertNotNull(value);
        assertEquals(0, value.size());
    }
}
