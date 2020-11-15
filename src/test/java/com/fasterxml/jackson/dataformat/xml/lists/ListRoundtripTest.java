package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class ListRoundtripTest extends XmlTestBase
{
    @JsonRootName("parents")
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

    @JsonRootName("point")
    static class Point {
        @JacksonXmlProperty(localName = "x", isAttribute = true)
        int x;
        @JacksonXmlProperty(localName = "y", isAttribute = true)
        int y;

        public Point() { }
        public Point(int x, int y) { this.x = x; this.y = y; }
    }

    @JsonRootName("Points")
    static class PointContainer {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "point")
        public List<Point> points;
    }

    // For [dataformat-xml#64]
    static class Optional {
        @JacksonXmlText
        public String number = "NOT SET";

        @JacksonXmlProperty(isAttribute=true)
        public String type = "NOT SET";
    }

    static class Optionals {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Optional> optional;
    } 
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    { // easier for eye, uncomment for testing
//        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
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

    // // [Issue#64]
    
    public void testOptionals() throws Exception
    {
        Optionals ob = MAPPER.readValue("<MultiOptional><optional type='work'>123-456-7890</optional></MultiOptional>",
                Optionals.class);
        assertNotNull(ob);
        assertNotNull(ob.optional);
        assertEquals(1, ob.optional.size());
//        System.err.println("ob: " + ob); // works fine
        Optional opt = ob.optional.get(0);
        assertEquals("123-456-7890", opt.number);
        assertEquals("work", opt.type);
    }

    /*// comment out for release
    public void testOptionalsWithMissingType() throws Exception
    {
//        Optionals ob = MAPPER.readValue("<MultiOptional><optional type='work'>123-456-7890</optional></MultiOptional>",
        Optionals ob = MAPPER.readValue("<MultiOptional><optional>123-456-7890</optional></MultiOptional>",
                Optionals.class);
        assertNotNull(ob);
        assertNotNull(ob.optional);
        assertEquals(1, ob.optional.size());

//            System.err.println("ob: " + ob); // works fine

        Optional opt = ob.optional.get(0);
        assertEquals("123-456-7890", opt.number);
        assertEquals("NOT SET", opt.type);
    }
*/    
}