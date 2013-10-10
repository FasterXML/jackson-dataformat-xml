package com.fasterxml.jackson.dataformat.xml.types;

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

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_OBJECT)
    protected static class BaseTypeWithClassObject { }

    protected static class SubTypeWithClassObject extends BaseTypeWithClassObject {
        public String name;
    
        public SubTypeWithClassObject() { }
        public SubTypeWithClassObject(String s) { name = s; }
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

    public void testAsClassProperty() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new SubTypeWithClassProperty("Foobar"));

        // Type info should be written as an attribute, so:
        /* 13-Jan-2010, tatu: With Jackson 1.7.1, it is possible to override type information
         *   inclusion, which allows use of attribute over element, so:
         */
        final String exp = 
            "<SubTypeWithClassProperty _class=\"com.fasterxml.jackson.dataformat.xml.types.TestPolymorphic..SubTypeWithClassProperty\">"
            //"<SubTypeWithClassProperty><_class>com.fasterxml.jackson.xml.types.TestPolymorphic..SubTypeWithClassProperty</_class>"
            +"<name>Foobar</name></SubTypeWithClassProperty>"
                ;
        assertEquals(exp, xml);
        
        Object result = _xmlMapper.readValue(xml, BaseTypeWithClassProperty.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassProperty.class, result.getClass());
        assertEquals("Foobar", ((SubTypeWithClassProperty) result).name);
    }
        
    public void testAsClassObject() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new SubTypeWithClassObject("Foobar"));
        Object result = _xmlMapper.readValue(xml, BaseTypeWithClassObject.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassObject.class, result.getClass());
        assertEquals("Foobar", ((SubTypeWithClassObject) result).name);
    }
}
   