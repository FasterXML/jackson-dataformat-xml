package com.fasterxml.jackson.dataformat.xml.ser.dos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.StreamWriteConstraints;

import com.fasterxml.jackson.databind.DatabindException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Simple unit tests to verify that we fail gracefully if you attempt to serialize
 * data that is cyclic (eg a list that contains itself).
 */
public class CyclicXMLDataSerTest extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

    public void testListWithSelfReference() throws Exception {
        // Avoid direct loop as serializer might be able to catch
        List<Object> list1 = new ArrayList<>();
        List<Object> list2 = new ArrayList<>();
        list1.add(list2);
        list2.add(list1);
        try {
            MAPPER.writeValueAsString(list1);
            fail("expected DatabindException for infinite recursion");
        } catch (DatabindException e) {
            String exceptionPrefix = String.format("Document nesting depth (%d) exceeds the maximum allowed",
                    StreamWriteConstraints.DEFAULT_MAX_DEPTH + 1);
            assertTrue("Exception message is as expected?",
                    e.getMessage().startsWith(exceptionPrefix));
        }
    }
}
