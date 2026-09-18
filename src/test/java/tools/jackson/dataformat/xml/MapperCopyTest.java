package tools.jackson.dataformat.xml;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.databind.*;

import static org.junit.jupiter.api.Assertions.*;

public class MapperCopyTest extends XmlTestUtil
{
    @JsonRootName("AnnotatedName")
    static class Pojo282
    {
        public int a = 3;
    }

    // [dataformat-xml#913]: separate types per test so that no cached
    // (de)serializer can hide effects of introspector changes
    static class ListBean913A {
        public List<String> values = new ArrayList<>(Arrays.asList("a", "b"));
    }

    static class ListBean913B {
        public List<String> values = new ArrayList<>(Arrays.asList("a", "b"));
    }

    static class ListBean913C {
        public List<String> values = new ArrayList<>(Arrays.asList("a", "b"));
    }

    static class CustomIntrospector913 extends JacksonXmlAnnotationIntrospector {
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void testMapperCopy()
    {
        XmlMapper mapper1 = mapperBuilder()
                .nameForTextElement("foo")
                .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();

        XmlMapper mapper2 = mapper1.rebuild().build();
        assertNotSame(mapper1, mapper2);

        XmlFactory xf1 = mapper1.tokenStreamFactory();
        XmlFactory xf2 = mapper2.tokenStreamFactory();
        // 21-May-2018, tatu: with 3.x, factories are immutable so complete fine to
        //    share
        assertSame(xf1, xf2);
        assertEquals(XmlFactory.class, xf2.getClass());

        // and incomplete copy as well
        assertEquals(xf1.getXMLTextElementName(), xf2.getXMLTextElementName());
        assertEquals(xf1.getFormatWriteFeatures(), xf2.getFormatWriteFeatures());
        assertEquals(xf1.getFormatReadFeatures(), xf2.getFormatReadFeatures());

        SerializationConfig sc1 = mapper1.serializationConfig();
        SerializationConfig sc2 = mapper2.serializationConfig();
        assertNotSame(sc1, sc2);
        assertEquals(
            sc1.getSerializationFeatures(),
            sc2.getSerializationFeatures(),
            "serialization features did not get copied"
        );
    }

    @Test
    public void testMapperSerialization() throws Exception
    {
        XmlMapper mapper1 = mapperBuilder()
                .nameForTextElement("foo")
                .build();
        assertEquals("foo", mapper1.tokenStreamFactory().getXMLTextElementName());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(bytes);
        objectStream.writeObject(mapper1);
        objectStream.close();
        
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        XmlMapper mapper2 = (XmlMapper) input.readObject();
        input.close();

        assertEquals("foo", mapper2.tokenStreamFactory().getXMLTextElementName());
    }

    // [dataformat-xml#282]
    @Test
    public void testCopyWith() throws Exception
    {
        XmlMapper xmlMapper = newMapper();
        final ObjectMapper xmlMapperNoAnno = xmlMapper.rebuild()
                .disable(MapperFeature.USE_ANNOTATIONS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();

        String xml1 = xmlMapper.writeValueAsString(new Pojo282());
        String xml2 = xmlMapperNoAnno.writeValueAsString(new Pojo282());

        if (!xml1.contains("AnnotatedName")) {
            fail("Should use name 'AnnotatedName', xml = "+xml1);
        }
        if (!xml2.contains("Pojo282")
                || xml2.contains("AnnotatedName")) {
            fail("Should NOT use name 'AnnotatedName' but 'Pojo282', xml = "+xml1);
        }
    }

    // [dataformat-xml#913]: `defaultUseWrapper()` of builder from `rebuild()` must
    // not change the (immutable) mapper that builder was created from
    @Test
    public void testRebuildWithDefaultUseWrapper() throws Exception
    {
        final String WRAPPED = "<ListBean913A><values><values>a</values><values>b</values></values></ListBean913A>";
        final String UNWRAPPED = "<ListBean913A><values>a</values><values>b</values></ListBean913A>";

        final XmlMapper mapper1 = newMapper();
        final XmlMapper mapper2 = mapper1.rebuild()
                .defaultUseWrapper(false)
                .build();

        assertEquals(UNWRAPPED, mapper2.writeValueAsString(new ListBean913A()));
        assertEquals(Arrays.asList("a", "b"),
                mapper2.readValue(UNWRAPPED, ListBean913A.class).values);

        // original mapper must keep using wrapping, for writing and reading
        assertEquals(WRAPPED, mapper1.writeValueAsString(new ListBean913A()));
        assertEquals(Arrays.asList("a", "b"),
                mapper1.readValue(WRAPPED, ListBean913A.class).values);
    }

    // [dataformat-xml#913]: ... nor mapper that builder itself built earlier
    @Test
    public void testDefaultUseWrapperAfterBuild() throws Exception
    {
        final XmlMapper.Builder b = mapperBuilder();
        final XmlMapper mapper1 = b.build();
        final XmlMapper mapper2 = b.defaultUseWrapper(false).build();

        assertEquals("<ListBean913B><values><values>a</values><values>b</values></values></ListBean913B>",
                mapper1.writeValueAsString(new ListBean913B()));
        assertEquals("<ListBean913B><values>a</values><values>b</values></ListBean913B>",
                mapper2.writeValueAsString(new ListBean913B()));
    }

    // [dataformat-xml#913]: copy used must retain introspector (sub-)type, and
    // other introspectors it may be paired with
    @Test
    public void testDefaultUseWrapperWithCustomIntrospectors() throws Exception
    {
        final CustomIntrospector913 xmlIntr = new CustomIntrospector913();
        final AnnotationIntrospector jaxbIntr = jakartaXMLBindAnnotationIntrospector();
        final XmlMapper mapper = mapperBuilder()
                .annotationIntrospector(XmlAnnotationIntrospector.Pair.instance(xmlIntr, jaxbIntr))
                .defaultUseWrapper(false)
                .build();

        List<AnnotationIntrospector> all = new ArrayList<>(
                mapper.serializationConfig().getAnnotationIntrospector().allIntrospectors());
        assertEquals(2, all.size());
        assertEquals(CustomIntrospector913.class, all.get(0).getClass());
        assertNotSame(xmlIntr, all.get(0));
        assertSame(jaxbIntr, all.get(1));

        assertEquals("<ListBean913C><values>a</values><values>b</values></ListBean913C>",
                mapper.writeValueAsString(new ListBean913C()));
    }
}
