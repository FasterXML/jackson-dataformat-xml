package com.fasterxml.jackson.dataformat.xml.lists;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class ListDeserializationTest extends XmlTestBase
{
    @JsonRootName(value = "person", namespace ="http://example.org/person" )
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
        @JsonAlias("aliasValue")
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

    // [dataformat-xml#256]
    static class ExampleObject256 {
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
        abstract List<ExampleObject256.LevelOne> getLevelOne();

        @JacksonXmlElementWrapper(useWrapping = false)
        @JsonProperty("LevelOne") // This is a workaround to set the element name, @JacksonXmlProperty seems to get ignored
        abstract void setLevelOne(List<ExampleObject256.LevelOne> levelOne);
    }

    static abstract class LevelOneMixin {
        @JacksonXmlProperty(localName = "LevelTwo")
        abstract ExampleObject256.LevelTwo getLevelTwo();

        @JsonProperty("LevelTwo")
        abstract void setLevelTwo(ExampleObject256.LevelTwo levelTwo);
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

    // [dataformat-xml#294]
    @JsonRootName("levels")
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

    @JsonRootName("Product")
    static class Product433 {
        @JsonProperty("Prices")
        public Prices433 prices;
    }

    // [dataformat-xml#307]
    @JsonRootName("customer")
    static class CustomerWithoutWrapper307 {
        public Long customerId;
        public String customerName;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Account307> account;
    }

    static class Account307 {
        public Long accountId;
        public String accountName;
        public String postcode;
    }

    // [dataformat-xml#433]
    static class Prices433 {
        @JsonProperty("Price")
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Price433> price;

        public List<Price433> getPrice() {
            if (price == null) {
                price = new ArrayList<Price433>();
            }
            return this.price;
        }
    }

    static class Price433 {
        @JsonProperty("Start")
        public Integer start;
        @JsonProperty("End")
        public Integer end;
        @JsonProperty("Price")
        public BigDecimal price;
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

    public void testUnwrappedAliasListBeanDeser() throws Exception
    {
        ListBeanUnwrapped bean = MAPPER.readValue(
                "<ListBeanUnwrapped><aliasValue>1</aliasValue><aliasValue>2</aliasValue><aliasValue>3</aliasValue></ListBeanUnwrapped>",
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

    // [dataformat-xml#256]
    public void testListWithMixinDeser256() throws Exception
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
                .addMixIn(ExampleObject256.class, ExampleObjectMixin.class)
                .addMixIn(ExampleObject256.LevelOne.class, LevelOneMixin.class)
                .addMixIn(ExampleObject256.LevelTwo.class, LevelTwoMixin.class)
                .build();
        ExampleObject256 result = mapper.readValue(XML, ExampleObject256.class);
        assertNotNull(result);
        assertNotNull(result.levelOne);
        assertEquals(1, result.levelOne.size());
        assertNotNull(result.levelOne.get(0));
        assertNotNull(result.levelOne.get(0).levelTwo);
        assertEquals("Value1", result.levelOne.get(0).levelTwo.fieldOne);
        assertEquals("Value2", result.levelOne.get(0).levelTwo.fieldTwo);
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

    // [dataformat-xml#307]
    public void testListDeser307() throws Exception
    {
        final String XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<customer xmlns=\"http://www.archer-tech.com/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <customerId>1</customerId>\n" +
                "    <customerName>Michael Judy</customerName>\n" +
                "    <account>\n" +
                "        <accountId>100</accountId>\n" +
                "        <accountName>Michael</accountName>\n" +
                "        <postcode xsi:nil=\"true\"></postcode>\n" +
                "    </account>\n" +
                "    <account>\n" +
                "        <accountId>200</accountId>\n" +
                "        <accountName>Judy</accountName>\n" +
                "        <postcode xsi:nil=\"true\"></postcode>\n" +
                "    </account> \n" +
                "</customer>";
        CustomerWithoutWrapper307 result =
                MAPPER.readValue(XML, CustomerWithoutWrapper307.class);
        assertNotNull(result);
        assertNotNull(result.account);
        assertEquals(2, result.account.size());
    }

    // [dataformat-xml#433]
    public void testListDeser433() throws Exception {
        final String XML =
"<Product>\n" +
" <Prices>\n" +
"  <Price>\n" +
"   <Start>50</Start>\n" +
"   <End>99</End>\n" +
"   <Price>2.53</Price>\n" +
"  </Price>\n" +
" </Prices>\n" +
"</Product>";

        Product433 main = MAPPER.readValue(XML, Product433.class);
        assertNotNull(main);
        assertNotNull(main.prices);
        Prices433 p = main.prices;
        assertNotNull(p.price);
        assertEquals(1, p.price.size());
        Price433 price = p.price.get(0);
        assertEquals(Integer.valueOf(99), price.end);
        assertEquals(new BigDecimal("2.53"), price.price);
    }
}
