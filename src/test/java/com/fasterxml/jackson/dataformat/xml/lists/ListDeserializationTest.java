package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class ListDeserializationTest extends XmlTestBase
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

    static class ListBeanWrapped
    {
        @JacksonXmlElementWrapper
        public List<Integer> values;
    }

    static class ListBeanUnwrapped
    {
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Integer> values;
    }

    // [dataformat-xml#191]
    static class TestList191 {
        @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
        @JacksonXmlProperty(localName = "item")
        public ArrayList<ListItem191> items;
    }    

    static class ListItem191 {
        @JacksonXmlProperty(isAttribute = true)
        public String name;
    }    

    // [dataformat-xml#294]
    @JacksonXmlRootElement(localName = "levels")
    static class RootLevel294 {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "sublevel")
        public List<Sublevel294> sublevels = new ArrayList<>();
    }

    @JsonPropertyOrder({ "id", "sublevel" })
    static class Sublevel294 {
        public Integer id;
        public String sublevel;
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

    public void testWrappedListBeanDeser() throws Exception
    {
        ListBeanWrapped bean = MAPPER.readValue(
                "<ListBeanWrapped><values><values>1</values><values>2</values><values>3</values></values></ListBeanWrapped>",
                ListBeanWrapped.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(3, bean.values.size());
        assertEquals(Integer.valueOf(1), bean.values.get(0));
        assertEquals(Integer.valueOf(2), bean.values.get(1));
        assertEquals(Integer.valueOf(3), bean.values.get(2));
    }

    // for [dataformat-xml#33]
    public void testWrappedListWithAttribute() throws Exception
    {
        ListBeanWrapped bean = MAPPER.readValue(
                "<ListBeanWrapped><values id='123'><values>1</values><values>2</values></values></ListBeanWrapped>",
                ListBeanWrapped.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        if (bean.values.size() < 2) { // preliminary check
            fail("List should have 2 entries, had "+bean.values.size());
        }
        assertEquals(Integer.valueOf(1), bean.values.get(0));
        assertEquals(Integer.valueOf(2), bean.values.get(1));
        assertEquals(2, bean.values.size());
    }

    public void testUnwrappedListBeanDeser() throws Exception
    {
        /*
        ListBeanUnwrapped foo = new ListBeanUnwrapped();
        foo.values = new ArrayList<Integer>();
        foo.values.add(1);
        foo.values.add(2);
        foo.values.add(3);
System.out.println("List -> "+MAPPER.writeValueAsString(foo));
*/
        
        ListBeanUnwrapped bean = MAPPER.readValue(
                "<ListBeanUnwrapped><values>1</values><values>2</values><values>3</values></ListBeanUnwrapped>",
                ListBeanUnwrapped.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(3, bean.values.size());
        assertEquals(Integer.valueOf(1), bean.values.get(0));
        assertEquals(Integer.valueOf(2), bean.values.get(1));
        assertEquals(Integer.valueOf(3), bean.values.get(2));
    }

    // [dataformat-xml#191]
    public void testListDeser191() throws Exception
    {
        final String XML =
"<TestList>\n"+
"    <items>\n"+
"        <item name='Item1'/>\n"+
"        <item name='Item2'> </item>\n"+ // important: at least one ws char between start/end
"        <item name='Item3'/>\n"+
"    </items>\n"+
"</TestList>"
                ;
        TestList191 testList = MAPPER.readValue(XML, TestList191.class);
        assertNotNull(testList);
        assertNotNull(testList.items);
        assertEquals(3, testList.items.size());
    }

    // [dataformat-xml#294]
    public void testNestedLists294() throws Exception
    {
        RootLevel294 tree = new RootLevel294();
        tree.sublevels.add(_newSublevel(1, "Name A"));
        tree.sublevels.add(_newSublevel(2, "Name B"));
        String xml = MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(tree);
//System.err.println("XML:\n"+xml);
        RootLevel294 resTree = MAPPER.readValue(xml, RootLevel294.class);
        assertNotNull(resTree);
        assertNotNull(resTree.sublevels);
        assertEquals(2, resTree.sublevels.size());
        assertEquals("Name B", resTree.sublevels.get(1).sublevel);
    }

    private Sublevel294 _newSublevel(Integer id, String sublevel) {
        Sublevel294 res = new Sublevel294();
        res.id = id;
        res.sublevel = sublevel;
        return res;
    }
}
