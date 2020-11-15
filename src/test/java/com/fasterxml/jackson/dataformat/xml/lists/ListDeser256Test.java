package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// [dataformat-xml#256]
public class ListDeser256Test extends XmlTestBase
{
    static class ExampleObject {
        public List<LevelOne> levelOne;

        public List<LevelOne> getLevelOne() { return levelOne; }

        static class LevelOne {
            public LevelTwo levelTwo;

            public LevelTwo getLevelTwo() { return levelTwo; }
        }
    
        static class LevelTwo {
            public String fieldOne;
            public String fieldTwo;

            public String getFieldOne() { return fieldOne; }
            public String getFieldTwo() { return fieldTwo; }
        }
    }

    @JsonRootName("Object")
    static abstract class ExampleObjectMixin {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "LevelOne")
        abstract List<ExampleObject.LevelOne> getLevelOne();

        @JacksonXmlElementWrapper(useWrapping = false)
        @JsonProperty("LevelOne") // This is a workaround to set the element name, @JacksonXmlProperty seems to get ignored
        abstract void setLevelOne(List<ExampleObject.LevelOne> levelOne);
    }

    static abstract class LevelOneMixin {
        @JacksonXmlProperty(localName = "LevelTwo")
        abstract ExampleObject.LevelTwo getLevelTwo();

        @JsonProperty("LevelTwo")
        abstract void setLevelTwo(ExampleObject.LevelTwo levelTwo);
    }

    static abstract class LevelTwoMixin {
        @JacksonXmlProperty(localName = "Field1")
        abstract String getFieldOne();

        @JsonProperty("Field1")
        abstract void setFieldOne(String fieldOne);

        @JacksonXmlProperty(localName = "Field2")
        abstract String getFieldTwo();

        @JsonProperty("Field2")
        abstract void setFieldTwo(String fieldTwo);
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    public void testDeser256() throws Exception
    {
        final String XML =
                "<Object>\n" + 
                "    <LevelOne> <!-- This is an array element -->\n" + 
                "        <LevelTwo>\n" + 
                "            <Field1>Value1</Field1>\n" + 
                "            <Field2>Value2</Field2>\n" + 
                "        </LevelTwo>\n" + 
                "    </LevelOne>\n" + 
                "</Object>";
        final XmlMapper mapper = XmlMapper.builder()
                .addMixIn(ExampleObject.class, ExampleObjectMixin.class)
                .addMixIn(ExampleObject.LevelOne.class, LevelOneMixin.class)
                .addMixIn(ExampleObject.LevelTwo.class, LevelTwoMixin.class)
                .build();
        ExampleObject result = mapper.readValue(XML, ExampleObject.class);
        assertNotNull(result);
        assertNotNull(result.levelOne);
        assertEquals(1, result.levelOne.size());
        assertNotNull(result.levelOne.get(0));
        assertNotNull(result.levelOne.get(0).levelTwo);
        assertEquals("Value1", result.levelOne.get(0).levelTwo.fieldOne);
        assertEquals("Value2", result.levelOne.get(0).levelTwo.fieldTwo);
    }
}
