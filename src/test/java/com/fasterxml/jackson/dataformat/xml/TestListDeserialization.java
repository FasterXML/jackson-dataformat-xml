package com.fasterxml.jackson.dataformat.xml;

import java.util.*;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class TestListDeserialization extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

	@JacksonXmlRootElement(localName = "person", namespace ="http://example.org/person" )
	public static class Person
	{
	   @JacksonXmlProperty( isAttribute = true )
	   public String id;
	   public String name;
	   public int age;

	   @JacksonXmlElementWrapper(localName = "notes")
	   @JacksonXmlProperty(localName = "note" )
	   public List<String> notes = new ArrayList<String>();
	   
	   public Person() { }
	   public Person(String name, int age) {
		   this.name = name;
		   this.age = age;
	   }
	}

    public static class PersonWithGetters
    {
       @JacksonXmlProperty( isAttribute = true )
       public String id;

       protected List<String> _notes = new ArrayList<String>();
              
       public PersonWithGetters() { }
       public PersonWithGetters(String id) {
           this.id = id;
       }

       @JacksonXmlElementWrapper(localName = "notes")
       @JacksonXmlProperty( localName = "note" )
       public List<String> getStuff() {
           return _notes;
       }

       public void setStuff(List<String> n) {
           _notes = n;
       }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();
    {
    	// easier for eye:
    	MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /* Issue#17 [https://github.com/FasterXML/jackson-dataformat-xml/issues/17]
     * 
     * Problems deserializing otherwise properly wrapped lists
     */
    public void testWrappedList() throws Exception
    {
    	Person p = new Person( "Name", 30 );
    	p.notes.add("note 1");
    	p.notes.add("note 2");
    	String xml = MAPPER.writeValueAsString( p );
    	Person result = MAPPER.readValue(xml, Person.class);
    	assertNotNull(result);
    	assertEquals("Name", result.name);
    	assertEquals(30, result.age);
    	assertEquals(2, result.notes.size());
    	assertEquals("note 1", result.notes.get(0));
    	assertEquals("note 2", result.notes.get(1));
    }

    public void testWrappedListWithGetters() throws Exception
    {
        PersonWithGetters p = new PersonWithGetters("abc");
        p._notes.add("note 1");
        p._notes.add("note 2");
        String xml = MAPPER.writeValueAsString( p );
        PersonWithGetters result = MAPPER.readValue(xml, PersonWithGetters.class);
        assertNotNull(result);
        assertEquals("abc", result.id);
        assertEquals(2, result._notes.size());
        assertEquals("note 1", result._notes.get(0));
        assertEquals("note 2", result._notes.get(1));
    }
}
