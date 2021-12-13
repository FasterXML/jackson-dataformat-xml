package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class UnwrappedPolymorphicList490Test extends XmlTestBase
{
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MyType490.class, name = "myType"),
    })
    interface IMyType490 { }

    static class MyType490 implements IMyType490 {
        public final String stringValue;
        public final Collection<String> typeNames;

        @JsonCreator
        public MyType490(
                @JsonProperty("stringValue") String stringValue,
                @JsonProperty("typeNames") Collection<String> typeNames) {
            this.stringValue = stringValue;
            this.typeNames = typeNames;
        }
    }

    // [dataformat-xml#490]
    public void testPolymorphicUnwrappedList490() throws Exception
    {
        XmlMapper xmlMapper = XmlMapper.builder()
                .defaultUseWrapper(false).build();

        List<String> typeNames = new ArrayList<>();
        typeNames.add("type1");
        typeNames.add("type2");
        MyType490 input = new MyType490("hello", typeNames);
        String doc = xmlMapper.writeValueAsString(input);
        IMyType490 result = xmlMapper.readValue(doc, IMyType490.class);        

        assertNotNull(result);
        assertEquals(MyType490.class, result.getClass());
        MyType490 typedResult = (MyType490) result;
        assertEquals(Arrays.asList("type1", "type2"), typedResult.typeNames);
    }
}
