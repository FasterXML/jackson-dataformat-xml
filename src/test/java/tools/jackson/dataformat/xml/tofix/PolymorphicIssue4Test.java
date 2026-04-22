package tools.jackson.dataformat.xml.tofix;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.*;


// Tests for lack of support for {@code JsonTypeInfo.As.WRAPPER_ARRAY}.
//
// [dataformat-xml#4]
// [dataformat-xml#9] (enums)
public class PolymorphicIssue4Test extends XmlTestUtil
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

    static enum TestEnum { A, B, C; }

    static class UntypedEnumBean
    {
       @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="__type")
// this would actually work:
//        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_OBJECT)
        public Object value;

        public UntypedEnumBean() { }
        public UntypedEnumBean(TestEnum v) { value = v; }
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
    /* Test methods
    /**********************************************************
     */

    protected final XmlMapper MAPPER = newMapper();

    /* 19-Dec-2010, tatu: Let's hold off these tests, due to issues with inclusions.
     */
    // Does not work since array wrapping is not explicitly forced (unlike with collection
    // property of a bean
    @JacksonTestFailureExpected
    @Test
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
    @JacksonTestFailureExpected
    @Test
    public void testAsWrappedClassArray() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new ClassArrayWrapper("Foobar"));
        ClassArrayWrapper result = MAPPER.readValue(xml, ClassArrayWrapper.class);
        assertNotNull(result);
        assertEquals(SubTypeWithClassArray.class, result.wrapped.getClass());
        assertEquals("Foobar", ((SubTypeWithClassArray) result.wrapped).name);
    }

    // [dataformat-xml#9]
    @JacksonTestFailureExpected
    @Test
    public void testUntypedEnum() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new UntypedEnumBean(TestEnum.B));

        UntypedEnumBean result = MAPPER.readValue(xml, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;

        if (TestEnum.class != ob.getClass()) {
            fail("Failed to deserialize TestEnum (got "+ob.getClass().getName()+") from: "+xml);
        }

        assertEquals(TestEnum.B, result.value);
    }
}
