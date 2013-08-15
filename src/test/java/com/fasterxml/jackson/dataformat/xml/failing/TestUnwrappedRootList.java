package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.*;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.*;

public class TestUnwrappedRootList extends XmlTestBase
{
    public static class SampleResource {
        private Long id;
        private String name;
        private String description;

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

    // for [Issue#38] -- root-level Collections not supported
    public void testListSerialization() throws Exception
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
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
            .withRootName("RootList")
            .writeValueAsString(l)
            .trim();

        // first trivial sanity checks
        assertNotNull(xml);
        if (xml.indexOf("<RootList>") < 0) {
            fail("Unexpected output: should have <RootList> as root element, got: "+xml);
        }

        // and then try reading back
        JavaType resListType = xmlMapper.getTypeFactory()
                .constructCollectionType(List.class, SampleResource.class);
        Object ob = xmlMapper.reader(resListType).readValue(xml);
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
}
