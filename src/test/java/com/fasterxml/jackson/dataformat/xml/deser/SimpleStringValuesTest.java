package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.deser.EmptyStringValueTest.Name;
import com.fasterxml.jackson.dataformat.xml.deser.EmptyStringValueTest.Names;

public class SimpleStringValuesTest extends XmlTestBase
{
    protected static class Bean2
    {
        public String a, b;

        @Override
        public String toString() {
            return "[a="+a+",b="+b+"]";
        }
    }

    static class Issue167Bean {
        public String d;
    }

    /*
    /**********************************************************
    /* Tests, basic
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();
    
    public void testSimpleStringElement() throws Exception
    {
        // first, simple one to verify baseline
        StringBean bean = MAPPER.readValue("<StringBean><text>text!</text></StringBean>", StringBean.class);
        assertNotNull(bean);
        assertEquals("text!", bean.text);
    }

    // 03-Jul-2020, tatu: This is actually not a good test starting with 2.12 as
    //   empty String can be used in two different ways... behavior change b/w 2.11
    //   and 2.12; 2.11 would lead to coercion from empty Object into default-ctor
    //   build Bean, but with gets empty String, passed via String-creator.
    // 06-Sep-2022, tatu: With 2.14 behavior should become closer to 2.11 in
    //   this respect.
    public void testMissingString() throws Exception
    {
        StringBean bean = MAPPER.readValue("<StringBean />", StringBean.class);
        assertNotNull(bean);
        assertEquals(new StringBean().text, bean.text);
    }

    /*
    /**********************************************************
    /* Tests, with attributes
    /**********************************************************
     */
    
    public void testStringWithAttribute() throws Exception
    {
        // and then the money shot: with 'standard' attribute...
        StringBean bean = MAPPER.readValue("<StringBean><text xml:lang='fi'>Pulla</text></StringBean>", StringBean.class);
        assertNotNull(bean);
        assertEquals("Pulla", bean.text);
    }

    public void testStringsWithAttribute() throws Exception
    {
        Bean2 bean = MAPPER.readValue(
                "<Bean2>\n"
                +"<a xml:lang='fi'>abc</a>"
                +"<b xml:lang='en'>def</b>"
//                +"<a>abc</a><b>def</b>"
                +"</Bean2>\n",
                Bean2.class);
        assertNotNull(bean);
        assertEquals("abc", bean.a);
        assertEquals("def", bean.b);
    }
    
    public void testStringArrayWithAttribute() throws Exception
    {
        // should even work for arrays of those
        StringBean[] beans = MAPPER.readValue(
                "<StringBean>\n"
                +"<StringBean><text xml:lang='fi'>Pulla</text></StringBean>"
                +"<StringBean><text xml:lang='se'>Bulla</text></StringBean>"
                +"<StringBean><text xml:lang='en'>Good stuff</text></StringBean>"
                +"</StringBean>",
                StringBean[].class);
        assertNotNull(beans);
        assertEquals(3, beans.length);
        assertEquals("Pulla", beans[0].text);
        assertEquals("Bulla", beans[1].text);
        assertEquals("Good stuff", beans[2].text);
    }

    public void testEmptyElementToString() throws Exception
    {
        final String XML =
"<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n"+
"<d xsi:nil='true'/>\n"+
"</a>\n";
        Issue167Bean result = MAPPER.readValue(XML, Issue167Bean.class);
        assertNotNull(result);
        // 06-Sep-2019, tatu: As per [dataformat-xml#354] this should now (2.10)
        //    produce real `null`:
//        assertEquals("", result.d);
        assertNull(result.d);
    }

    /*
    /**********************************************************
    /* Tests, Lists
    /**********************************************************
     */
    
    public void testStringsInList() throws Exception
    {
        Names input = new Names();
        input.names.add(new Name("Bob", "Lee"));
        input.names.add(new Name("", ""));
        input.names.add(new Name("Sponge", "Bob"));
        String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        
//System.err.println("XML:\n"+xml);

        Names result = MAPPER.readValue(xml, Names.class);
        assertNotNull(result);
        assertNotNull(result.names);
        assertEquals(3, result.names.size());
        assertEquals("Bob", result.names.get(2).last);

        // [dataformat-xml#162]: should get empty String, not null
        assertEquals("", result.names.get(1).first);
    }
}
