package com.fasterxml.jackson.dataformat.xml.misc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class PolymorphicTypesTest extends XmlTestBase
{
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

    // [dataformat-xml#451]
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(@JsonSubTypes.Type(Child451.class))
    public interface Value451 {}

    public static class Child451 implements Value451 {
        private final String property1;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Child451(@JsonProperty("property1") String property1) {
            this.property1 = property1;
        }

        public String getProperty1() {
            return property1;
        }
    }    

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testAsClassProperty() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new SubTypeWithClassProperty("Foobar"));

        // Type info should be written as an attribute, so:
        final String exp = 
            "<SubTypeWithClassProperty _class=\"com.fasterxml.jackson.dataformat.xml.misc.PolymorphicTypesTest..SubTypeWithClassProperty\">"
            //"<SubTypeWithClassProperty><_class>com.fasterxml.jackson.xml.types.TestPolymorphic..SubTypeWithClassProperty</_class>"
            +"<name>Foobar</name></SubTypeWithClassProperty>"
                ;
        assertEquals(exp, xml);
        
        Object result = MAPPER.readValue(xml, BaseTypeWithClassProperty.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassProperty.class, result.getClass());
        assertEquals("Foobar", ((SubTypeWithClassProperty) result).name);
    }
        
    public void testAsClassObject() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new SubTypeWithClassObject("Foobar"));
        Object result = MAPPER.readValue(xml, BaseTypeWithClassObject.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassObject.class, result.getClass());
        assertEquals("Foobar", ((SubTypeWithClassObject) result).name);
    }

    // Test for [dataformat-xml#81]
    public void testAsPropertyWithObjectId() throws Exception
    {
        List<TypeWithClassPropertyAndObjectId> data = new ArrayList<PolymorphicTypesTest.TypeWithClassPropertyAndObjectId>();
        TypeWithClassPropertyAndObjectId object = new TypeWithClassPropertyAndObjectId("Foobar");
        data.add(object);
        // This will be written as an id reference instead of object; as such, no type info will be written.
        data.add(object);
        String xml = MAPPER.writeValueAsString(new Wrapper(data));
        Wrapper result = MAPPER.readValue(xml, Wrapper.class);
        assertNotNull(result);
        assertSame(result.data.get(0), result.data.get(1));
        assertEquals("Foobar", result.data.get(0).id);
    }

    // Test for [dataformat-xml#451]
    public void testDeduction() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Child451("value1"));
        assertTrue(xml.contains("<property1>value1</property1>"));

        // and try reading back for funsies
        Value451 result = MAPPER.readValue(xml, Value451.class);
        assertNotNull(result);
        assertEquals(Child451.class, result.getClass());
    }
}
   