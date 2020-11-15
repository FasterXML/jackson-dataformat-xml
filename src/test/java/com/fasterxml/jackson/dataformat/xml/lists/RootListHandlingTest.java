package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Tests for verifying that Lists (and arrays) can be serialized even
 * when they are root values.
 */
public class RootListHandlingTest extends XmlTestBase
{
    @JsonRootName("SR")
    @JsonPropertyOrder({ "id", "name", "description" })
    public static class SampleResource {
        private Long id;
        private String name;
        private String description;

        public SampleResource() { }
        public SampleResource(long id, String n, String d) {
            this.id = id;
            name = n;
            description = d;
        }
        
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // Test for ensuring that we can use ".withRootName()" to override
    // default name AND annotation
    public void testRenamedRootItem() throws Exception
    {
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper
                .writer()
                .withRootName("Shazam")
                .writeValueAsString(new SampleResource(123, "Foo", "Barfy!"))
                .trim();
        xml = removeSjsxpNamespace(xml);
        assertEquals("<Shazam><id>123</id><name>Foo</name><description>Barfy!</description></Shazam>", xml);
    }
    
    // for [Issue#38] -- root-level Collections not supported
    public void testListSerialization() throws Exception
    {
        _testListSerialization(true);
        _testListSerialization(false);
    }
        
    private void _testListSerialization(boolean useWrapping) throws Exception
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(useWrapping);
        XmlMapper xmlMapper = new XmlMapper(module);
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        xmlMapper.setAnnotationIntrospector(introspector);

        SampleResource r1 = new SampleResource();
        r1.setId(123L);
        r1.setName("Albert");
        r1.setDescription("desc");

        SampleResource r2 = new SampleResource();
        r2.setId(123L);
        r2.setName("William");
        r2.setDescription("desc2");

        List<SampleResource> l = new ArrayList<SampleResource>();
        l.add(r1);
        l.add(r2);

        // to see what JAXB might do, uncomment:
//System.out.println("By JAXB: "+jaxbSerialized(l)); //  ArrayList.class, SampleResource.class));

        String xml = xmlMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(l)
            .trim();

        // first trivial sanity checks
        assertNotNull(xml);
        if (xml.indexOf("<ArrayList>") < 0) {
            fail("Unexpected output: should have <ArrayList> as root element, got: "+xml);
        }

        // and then try reading back
        JavaType resListType = xmlMapper.getTypeFactory()
                .constructCollectionType(List.class, SampleResource.class);
        Object ob = xmlMapper.readerFor(resListType).readValue(xml);
        assertNotNull(ob);

//      System.err.println("XML -> "+xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ob));
        
        assertTrue(ob instanceof List);
        List<?> resultList = (List<?>) ob;
        assertEquals(2, resultList.size());
        assertEquals(SampleResource.class, resultList.get(0).getClass());
        assertEquals(SampleResource.class, resultList.get(1).getClass());
        SampleResource rr = (SampleResource) resultList.get(1);
        assertEquals("William", rr.getName());
    }

    // Related to #38 as well
    public void testArraySerialization() throws Exception
    {
        _testArraySerialization(true);
        _testArraySerialization(false);
    }
    
    private void _testArraySerialization(boolean useWrapping) throws Exception
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(useWrapping);
        XmlMapper xmlMapper = new XmlMapper(module);
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        xmlMapper.setAnnotationIntrospector(introspector);

        SampleResource r1 = new SampleResource();
        r1.setId(123L);
        r1.setName("Albert");
        r1.setDescription("desc");

        SampleResource r2 = new SampleResource();
        r2.setId(123L);
        r2.setName("William");
        r2.setDescription("desc2");

        SampleResource[] input = new SampleResource[] { r1, r2 };

        // to see what JAXB might do, uncomment:
//System.out.println("By JAXB: "+jaxbSerialized(input));

        String xml = xmlMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input)
            .trim();

        // first trivial sanity checks
        assertNotNull(xml);
        // Is this good name? If not, what should be used instead?
        if (xml.indexOf("<SampleResources>") < 0) {
            fail("Unexpected output: should have <SampleResources> as root element, got: "+xml);
        }

        // and then try reading back
        SampleResource[] result = xmlMapper.readerFor(SampleResource[].class).readValue(xml);
        assertNotNull(result);

//      System.err.println("XML -> "+xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ob));
        
        assertEquals(2, result.length);
        SampleResource rr = result[1];
        assertEquals("desc2", rr.getDescription());
    }

}
