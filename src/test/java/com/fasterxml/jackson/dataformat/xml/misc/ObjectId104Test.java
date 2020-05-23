package com.fasterxml.jackson.dataformat.xml.misc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Unit test to showcase issue #104, based on TestObjectIdDeserialization unit test in databind package.
 */
public class ObjectId104Test extends XmlTestBase {
    // // Classes for external id from property annotations:
    
    static class IdWrapper
    {
        @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="id")
        public ValueNode node;

        public IdWrapper() { }
        public IdWrapper(int v) {
            node = new ValueNode(v);
        }
    }

    static class ValueNode {
        public List<Integer> value = new ArrayList<Integer>();
        public IdWrapper next;
        
        public ValueNode() { this(0); }
        public ValueNode(int v) { value.add(v); }
    }

    private final XmlMapper MAPPER = newMapper();

    // Another test to ensure ordering is not required (i.e. can do front references)
    public void testSimpleCollectionDeserWithForwardRefs() throws Exception
    {
        IdWrapper result = MAPPER.readValue("<IdWrapper><node><value><value>7</value></value><next><node>1</node></next><id>1</id></node></IdWrapper>"
                ,IdWrapper.class);
        assertEquals(7, (int)result.node.value.get(0));
        assertSame(result.node, result.node.next.node);
    }
}
