package com.fasterxml.jackson.dataformat.xml.failing;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PolymorphicList426Test extends XmlTestBase
{
    static class Auto {
        @JacksonXmlProperty(localName = "Object")
        @JacksonXmlElementWrapper(useWrapping = false)
//        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<CarParts> carParts;
    }

    @JsonTypeIdResolver(CarPartsResolver.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
    abstract static class CarParts {
        @JacksonXmlProperty(isAttribute = true) // The is triggering the issue.
        public String uid;
    
        @JacksonXmlProperty(localName = "Object")
        @JacksonXmlElementWrapper(useWrapping = false)
//        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<CarParts> carParts;
    }

    static class Engine extends CarParts{}
    static class Chassis extends CarParts{}
    static class Motor extends CarParts{}
    static class Body extends CarParts{}

    static class CarPartsResolver extends TypeIdResolverBase {
        private JavaType superType;

        @Override
        public void init(JavaType javaType) {
            this.superType = javaType;
        }

        @Override
        public String idFromValue(Object o) {
            return idFromValueAndType(o, o.getClass());
        }

        @Override
        public String idFromValueAndType(Object o, Class<?> aClass) {
            return aClass.getSimpleName();
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) throws IOException {
            Class<?> subType = null;
            switch (id) {
                case "Engine": subType = Engine.class; break;
                case "Chassis": subType = Chassis.class; break;
                case "Motor": subType = Motor.class; break;
                case "Body": subType = Body.class; break;
            }
            return context.constructSpecializedType(superType, subType);
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#426]
    public void testPolymorphicList426() throws Exception
    {
        String xml = "" +
"<Auto>\n" +
"  <Object uid='1' type='Engine'>\n" +
"    <Object uid='2' type='Chassis'></Object>\n" +
"    <Object uid='3' type='Motor'></Object>\n" +
"  </Object>\n" +
"  <Object uid='4' type='Body'></Object>\n" +
"</Auto>";
        Auto result = MAPPER.readValue(xml, Auto.class);
        assertNotNull(result);
        assertNotNull(result.carParts);
        assertEquals(2, result.carParts.size());
        CarParts cp = result.carParts.get(0);

        // for debugging:
//System.err.println("XML:\n"+MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        assertNotNull(cp);
        assertNotNull(cp.carParts);

        // So far so good, but fails here:
        assertEquals(2, cp.carParts.size());
    }
}
