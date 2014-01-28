package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

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
}
   