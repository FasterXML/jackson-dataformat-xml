package com.fasterxml.jackson.dataformat.xml.misc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class PolymorphicTypesTest extends XmlTestBase
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

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_OBJECT)
    protected static class BaseTypeWithClassObject { }

    protected static class SubTypeWithClassObject extends BaseTypeWithClassObject {
        public String name;
    
        public SubTypeWithClassObject() { }
        public SubTypeWithClassObject(String s) { name = s; }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    protected static class TypeWithClassPropertyAndObjectId {
        public String id;

        public TypeWithClassPropertyAndObjectId() {}
        public TypeWithClassPropertyAndObjectId(String id) { this.id = id; }
    }

    protected static class Wrapper {
        public List<TypeWithClassPropertyAndObjectId> data;

        public Wrapper(){}
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

    public void testAsClassProperty() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new SubTypeWithClassProperty("Foobar"));

        // Type info should be written as an attribute, so:
        final String exp = 
            "<SubTypeWithClassProperty _class=\"com.fasterxml.jackson.dataformat.xml.misc.PolymorphicTypesTest..SubTypeWithClassProperty\">"
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

    /**
     * Test for [dataformat-xml#81]
     */
    public void testAsPropertyWithObjectId() throws Exception
    {
        List<TypeWithClassPropertyAndObjectId> data = new ArrayList<PolymorphicTypesTest.TypeWithClassPropertyAndObjectId>();
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
   