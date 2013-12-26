package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.dataformat.xml.*;

public class TestPolymorphic extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY)
    static class BaseTypeWithClassProperty { }

    static class SubTypeWithClassProperty extends BaseTypeWithClassProperty {
        public String name;

        public SubTypeWithClassProperty() { }
        public SubTypeWithClassProperty(String s) { name = s; }
    }
    
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_ARRAY)
    static class BaseTypeWithClassArray { }

    static class SubTypeWithClassArray extends BaseTypeWithClassArray {
        public String name;

        public SubTypeWithClassArray() { }
        public SubTypeWithClassArray(String s) { name = s; }
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_OBJECT)
    static class BaseTypeWithClassObject { }

    static class SubTypeWithClassObject extends BaseTypeWithClassObject {
        public String name;
    
        public SubTypeWithClassObject() { }
        public SubTypeWithClassObject(String s) { name = s; }
    }
    
    /**
     * If not used as root element, need to use a wrapper
     */
    static class ClassArrayWrapper
    {
        public BaseTypeWithClassArray wrapped;

        public ClassArrayWrapper() { }
        public ClassArrayWrapper(String s) { wrapped = new SubTypeWithClassArray(s); }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    protected static class TypeWithClassPropertyAndObjectId {
        public String id;

        public TypeWithClassPropertyAndObjectId(String id) { this.id = id; }
    }

    protected static class Wrapper {
        public List<TypeWithClassPropertyAndObjectId> data;

        public Wrapper(List<TypeWithClassPropertyAndObjectId> data) { this.data = data; }
    }
    
    /*
    /**********************************************************
    /* Set up
    /**********************************************************
     */

    protected XmlMapper _xmlMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _xmlMapper = new XmlMapper();
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    /* 19-Dec-2010, tatu: Let's hold off these tests, due to issues with inclusions.
     */
    // Does not work since array wrapping is not explicitly forced (unlike with collection
    // property of a bean
    public void testAsClassArray() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new SubTypeWithClassArray("Foobar"));
        Object result = _xmlMapper.readValue(xml, BaseTypeWithClassArray.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassArray.class, result.getClass());
        assertEquals("Foobar", ((SubTypeWithClassArray) result).name);
    }

    // Hmmh. Does not yet quite work either, since we do not properly force
    // array context when writing...
    public void testAsWrappedClassArray() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new ClassArrayWrapper("Foobar"));
        ClassArrayWrapper result = _xmlMapper.readValue(xml, ClassArrayWrapper.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassArray.class, result.wrapped.getClass());
        assertEquals("Foobar", ((SubTypeWithClassArray) result.wrapped).name);
    }

    /**
     * Test for issue 81
     */
    public void testAsPropertyWithObjectId() throws Exception
    {
        List<TypeWithClassPropertyAndObjectId> data = new ArrayList<TestPolymorphic.TypeWithClassPropertyAndObjectId>();
        TypeWithClassPropertyAndObjectId object = new TypeWithClassPropertyAndObjectId("Foobar");
        data.add(object);
        // This will be written as an id reference instead of object; as such, no type info will be written.
        data.add(object);
        String xml = _xmlMapper.writeValueAsString(new Wrapper(data));
        Wrapper result = _xmlMapper.readValue(xml, Wrapper.class);
        assertNotNull(result);
        assertSame(result.data.get(0), result.data.get(1));
        assertEquals("Foobar", result.data.get(0).id);
    }
}
   