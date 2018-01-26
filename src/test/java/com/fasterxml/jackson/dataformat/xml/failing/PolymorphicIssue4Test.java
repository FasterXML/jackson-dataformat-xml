package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// for:
//
// [dataformat-xml#4]
// [dataformat-xml#9] (enums)

public class PolymorphicIssue4Test extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    /*
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY)
    static class BaseTypeWithClassProperty { }

    static class SubTypeWithClassProperty extends BaseTypeWithClassProperty {
        public String name;

        public SubTypeWithClassProperty() { }
        public SubTypeWithClassProperty(String s) { name = s; }
    }
    */

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
    /* Test methods
    /**********************************************************
     */

    protected final XmlMapper MAPPER = newMapper();

    /* 19-Dec-2010, tatu: Let's hold off these tests, due to issues with inclusions.
     */
    // Does not work since array wrapping is not explicitly forced (unlike with collection
    // property of a bean
    public void testAsClassArray() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new SubTypeWithClassArray("Foobar"));
        Object result = MAPPER.readValue(xml, BaseTypeWithClassArray.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassArray.class, result.getClass());
        assertEquals("Foobar", ((SubTypeWithClassArray) result).name);
    }

    // Hmmh. Does not yet quite work either, since we do not properly force
    // array context when writing...
    public void testAsWrappedClassArray() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new ClassArrayWrapper("Foobar"));
        ClassArrayWrapper result = MAPPER.readValue(xml, ClassArrayWrapper.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassArray.class, result.wrapped.getClass());
        assertEquals("Foobar", ((SubTypeWithClassArray) result.wrapped).name);
    }
}
