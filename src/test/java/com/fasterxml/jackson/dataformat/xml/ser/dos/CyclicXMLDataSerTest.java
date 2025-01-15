package com.fasterxml.jackson.dataformat.xml.ser.dos;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CyclicXMLDataSerTest extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
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
            assertTrue(e.getMessage().startsWith(exceptionPrefix),
                    "Exception message is as expected?");
        }
    }
}
