package com.fasterxml.jackson.dataformat.xml.deser.builder;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class BuilderSimpleTest extends XmlTestBase
{
    // // Simple 2-property value class, builder with standard naming

    @JsonDeserialize(builder=SimpleBuilderXY.class)
    static class ValueClassXY
    {
        final int _x, _y;

        protected ValueClassXY(int x, int y) {
            _x = x+1;
            _y = y+1;
        }
    }

    static class SimpleBuilderXY
    {
        public int x, y;
    	
        public SimpleBuilderXY withX(int x0) {
    		    this.x = x0;
    		    return this;
        }

        public SimpleBuilderXY withY(int y0) {
    		    this.y = y0;
    		    return this;
        }

        public ValueClassXY build() {
    		    return new ValueClassXY(x, y);
        }
    }

    // // 3-property value, with more varied builder

    @JsonDeserialize(builder=BuildABC.class)
    static class ValueClassABC
    {
        final int a, b, c;

        protected ValueClassABC(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    @JsonIgnoreProperties({ "d" })
    static class BuildABC
    {
        public int a; // to be used as is
        private int b, c;
    	
        @JsonProperty("b")
        public BuildABC assignB(int b0) {
            this.b = b0;
            return this;
        }

        // Also ok NOT to return 'this'
        @JsonSetter("c")
        public void c(int c0) {
            this.c = c0;
        }

        public ValueClassABC build() {
            return new ValueClassABC(a, b, c);
        }
    }

    // // Then Builder that is itself immutable
    
    @JsonDeserialize(builder=BuildImmutable.class)
    static class ValueImmutable
    {
        final int value;
        protected ValueImmutable(int v) { value = v; }
    }
    
    static class BuildImmutable {
        private final int value;
        
        private BuildImmutable() { this(0); }
        private BuildImmutable(int v) {
            value = v;
        }
        public BuildImmutable withValue(int v) {
            return new BuildImmutable(v);
        }
        public ValueImmutable build() {
            return new ValueImmutable(value);
        }
    }
    // And then with custom naming:

    @JsonDeserialize(builder=BuildFoo.class)
    static class ValueFoo
    {
        final int value;
        protected ValueFoo(int v) { value = v; }
    }

    @JsonPOJOBuilder(withPrefix="foo", buildMethodName="construct")
    static class BuildFoo {
        private int value;
        
        public BuildFoo fooValue(int v) {
            value = v;
            return this;
        }
        public ValueFoo construct() {
            return new ValueFoo(value);
        }
    }


    // for [databind#761]

    @JsonDeserialize(builder=ValueInterfaceBuilder.class)
    interface ValueInterface {
        int getX();
    }

    @JsonDeserialize(builder=ValueInterface2Builder.class)
    interface ValueInterface2 {
        int getX();
    }
    
    static class ValueInterfaceImpl implements ValueInterface
    {
        final int _x;

        protected ValueInterfaceImpl(int x) {
            _x = x+1;
        }

        @Override
        public int getX() {
            return _x;
        }
    }

    static class ValueInterface2Impl implements ValueInterface2
    {
        final int _x;

        protected ValueInterface2Impl(int x) {
            _x = x+1;
        }

        @Override
        public int getX() {
            return _x;
        }
    }
    
    static class ValueInterfaceBuilder
    {
        public int x;

        public ValueInterfaceBuilder withX(int x0) {
            this.x = x0;
            return this;
        }

        public ValueInterface build() {
            return new ValueInterfaceImpl(x);
        }
    }

    static class ValueInterface2Builder
    {
        public int x;

        public ValueInterface2Builder withX(int x0) {
            this.x = x0;
            return this;
        }

        // should also be ok: more specific type
        public ValueInterface2Impl build() {
            return new ValueInterface2Impl(x);
        }
    }

    // [databind#777]
    @JsonDeserialize(builder = SelfBuilder777.class)
    @JsonPOJOBuilder(buildMethodName = "", withPrefix = "with")
    static class SelfBuilder777 {
        public int x;

        public SelfBuilder777 withX(int value) {
            x = value;
            return this;
        }
    }

    // Won't work well with XML, omit
    // [databind#822]
    /*
    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    static class ValueBuilder822

    @JsonDeserialize(builder = ValueBuilder822.class)
    static class ValueClass822
    */

    protected static class NopModule1557 extends com.fasterxml.jackson.databind.Module
    {
        @Override
        public String getModuleName() {
            return "NopModule";
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext setupContext) {
            // This annotation introspector has no opinion about builders, make sure it doesn't interfere
            setupContext.insertAnnotationIntrospector(new NopAnnotationIntrospector() {
                private static final long serialVersionUID = 1L;
                @Override
                public Version version() {
                    return Version.unknownVersion();
                }
            });
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testSimple() throws Exception
    {
        String doc = "<ValueClassXY><x>1</x><y>2</y></ValueClassXY>";
        Object o = MAPPER.readValue(doc, ValueClassXY.class);
        assertNotNull(o);
        assertSame(ValueClassXY.class, o.getClass());
        ValueClassXY value = (ValueClassXY) o;
        // note: ctor adds one to both values
        assertEquals(value._x, 2);
        assertEquals(value._y, 3);
    }

    // related to [databind#1214]
    public void testSimpleWithIgnores() throws Exception
    {
        // 'z' is unknown, and would fail by default:
        String doc = "<ValueClassXY><x>1</x><y>2</y><z>4</z></ValueClassXY>";
        Object o = null;

        try {
            o = MAPPER.readValue(doc, ValueClassXY.class);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            assertEquals("z", e.getPropertyName());
            verifyException(e, "Unrecognized field \"z\"");
        }

        // but with config overrides should pass
        ObjectMapper ignorantMapper = newMapper();
        ignorantMapper.configOverride(SimpleBuilderXY.class)
                .setIgnorals(JsonIgnoreProperties.Value.forIgnoreUnknown(true));
        o = ignorantMapper.readValue(doc, ValueClassXY.class);
        assertNotNull(o);
        assertSame(ValueClassXY.class, o.getClass());
        ValueClassXY value = (ValueClassXY) o;
        // note: ctor adds one to both values
        assertEquals(value._x, 2);
        assertEquals(value._y, 3);
    }
    
    public void testMultiAccess() throws Exception
    {
        String doc = "<ValueClassABC><c>3</c>  <a>2</a>  <b>-9</b></ValueClassABC>";
        ValueClassABC value = MAPPER.readValue(doc, ValueClassABC.class);
        assertNotNull(value);
        assertEquals(2, value.a);
        assertEquals(-9, value.b);
        assertEquals(3, value.c);

        // also, since we can ignore some properties:
        value = MAPPER.readValue("<ValueClassABC><c>3</c>\n"
                +"<d>5</d><b>-9</b></ValueClassABC>",
                ValueClassABC.class);
        assertNotNull(value);
        assertEquals(0, value.a);
        assertEquals(-9, value.b);
        assertEquals(3, value.c);
    }

    // test for Immutable builder, to ensure return value is used
    public void testImmutable() throws Exception
    {
        ValueImmutable value = MAPPER.readValue("<ValueImmutable><value>13</value></ValueImmutable>",
                ValueImmutable.class);        
        assertEquals(13, value.value);
    }

    // test with custom 'with-prefix'
    public void testCustomWith() throws Exception
    {
        ValueFoo value = MAPPER.readValue("<ValueFoo><value>1</value></ValueFoo>", ValueFoo.class);        
        assertEquals(1, value.value);
    }

    // for [databind#761]
    
    public void testBuilderMethodReturnMoreGeneral() throws Exception
    {
        ValueInterface value = MAPPER.readValue("<ValueInterface><x>1</x></ValueInterface>", ValueInterface.class);
        assertEquals(2, value.getX());
    }

    public void testBuilderMethodReturnMoreSpecific() throws Exception
    {
        final String json = "<ValueInterface2><x>1</x></ValueInterface2>}";
        ValueInterface2 value = MAPPER.readValue(json, ValueInterface2.class);
        assertEquals(2, value.getX());
    }

    public void testSelfBuilder777() throws Exception
    {
        SelfBuilder777 result = MAPPER.readValue("<SelfBuilder777><x>3</x></SelfBuilder777>'",
                SelfBuilder777.class);
        assertNotNull(result);
        assertEquals(3, result.x);
    }

    // Won't work well with XML, omit:
//    public void testWithAnySetter822() throws Exception

    public void testPOJOConfigResolution1557() throws Exception
    {
        final String json = "<ValueFoo><value>1</value></ValueFoo>";
        MAPPER.registerModule(new NopModule1557());
        ValueFoo value = MAPPER.readValue(json, ValueFoo.class);
        assertEquals(1, value.value);
    }
}
