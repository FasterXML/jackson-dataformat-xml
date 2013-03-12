package com.fasterxml.jackson.dataformat.xml.unwrapped;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestListRoundtrip extends XmlTestBase
{
    @JacksonXmlRootElement(localName="parents")
    public static class Parents {
      @JacksonXmlElementWrapper(useWrapping=false)
      public List<Parent> parent = new ArrayList<Parent>();
    }

    @JsonPropertyOrder({ "name", "desc", "prop" })
    public static class Parent {
      @JacksonXmlProperty(isAttribute=true)
      public String name;

      public String description;
      
      @JacksonXmlElementWrapper(useWrapping=false)
      public List<Prop> prop = new ArrayList<Prop>();

      public Parent() { }
      public Parent(String name, String desc) {
          this.name = name;
          description = desc;
      }
    }

    static class Prop {
      @JacksonXmlProperty(isAttribute=true)
      public String name;

      public String value;

      public Prop() { }
      public Prop(String name, String value) {
          this.name = name;
          this.value = value;
      }
    }

    // For [Issue#58]:
    @JacksonXmlRootElement(localName = "point")
    static class Point {
        @JacksonXmlProperty(localName = "x", isAttribute = true)
        int x;
        @JacksonXmlProperty(localName = "y", isAttribute = true)
        int y;

        public Point() { }
        public Point(int x, int y) { this.x = x; this.y = y; }
    }

    @JacksonXmlRootElement(localName = "Points")
    static class PointContainer {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "point")
        public List<Point> points;
    }

    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    { // easier for eye:
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public void testParentListRoundtrip() throws Exception
    {
        Parents root = new Parents();
        Parent parent1 = new Parent("a", "First");
        root.parent.add(parent1);
        parent1.prop.add(new Prop("width", "13"));
        parent1.prop.add(new Prop("height", "10"));
        Parent parent2 = new Parent("b", "Second");
        parent2.prop.add(new Prop("x", "1"));
        parent2.prop.add(new Prop("y", "2"));
        root.parent.add(parent2);

        String xml = MAPPER.writeValueAsString(root);
        assertNotNull(xml);

        // then bring it back
        Parents result = MAPPER.readValue(xml, Parents.class);
        assertNotNull(result.parent);
        assertEquals(2, result.parent.size());
        Parent p2 = result.parent.get(1);
        assertNotNull(p2);
        assertEquals("b", p2.name);
        assertEquals("Second", p2.description);

        assertEquals(2, p2.prop.size());
        Prop prop2 = p2.prop.get(1);
        assertNotNull(prop2);
        assertEquals("2", prop2.value);
    }

    public void testListWithAttrOnlyValues() throws Exception
    {
        PointContainer obj = new PointContainer();
        obj.points = new ArrayList<Point>();
        obj.points.add(new Point(1, 2));
        obj.points.add(new Point(3, 4));
        obj.points.add(new Point(5, 6));

        String xml = MAPPER.writeValueAsString(obj);

        PointContainer converted = MAPPER.readValue(xml, PointContainer.class);

        assertEquals(3, converted.points.size());
        assertNotNull(converted.points.get(0));
        assertNotNull(converted.points.get(1));
        assertNotNull(converted.points.get(2));

        assertEquals(2, converted.points.get(0).y);
        assertEquals(4, converted.points.get(1).y);
        assertEquals(6, converted.points.get(2).y);
    }
    
}