package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

// for [dataformat-xml#55]
public class ListAnnotationSharingTest extends XmlTestBase
{
    static class Wrapper {
        @JacksonXmlElementWrapper(localName = "Points", useWrapping = true)
        @JsonProperty("Point")
        List<Point> points = new ArrayList<Point>();

        public List<Point> getPoints() {
            return points;
        }
    }

    @JsonPropertyOrder({"x", "y"})
    static class Point {
        public int x, y;

        public Point() { }
        public Point(int x, int y) { this.x = x;
            this.y = y;
        }
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    
     public void testAnnotationSharing() throws Exception
     {
         Wrapper input = new Wrapper();
         input.points.add(new Point(1, 2));
         String xml = MAPPER.writeValueAsString(input);

         assertEquals("<Wrapper><Points><Point><x>1</x><y>2</y></Point></Points></Wrapper>", xml);

         // and then back
         Wrapper result = MAPPER.readValue(xml, Wrapper.class);
         assertEquals(1, result.points.size());
     }
    
}
