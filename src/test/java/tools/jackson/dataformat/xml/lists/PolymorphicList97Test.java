package tools.jackson.dataformat.xml.lists;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author pgelinas
 */
public class PolymorphicList97Test extends XmlTestUtil
{
    @JsonTypeInfo(property = "type", use = Id.NAME)
    public static abstract class Foo {
        @JacksonXmlProperty(isAttribute = true)
        public String data;
    }

    @JsonTypeName("good")
    public static class FooGood extends Foo {
        public String bar;
    }

    @JsonTypeName("bad")
    public static class FooBad extends Foo {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> bar;
    }

    @Test
    public void testGood() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .registerSubtypes(FooGood.class)
                .build();
        String xml = "<Foo type=\"good\" data=\"dummy\"><bar>FOOBAR</bar></Foo>";
        Foo fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooGood.class));

        xml = "<Foo data=\"dummy\" type=\"good\" ><bar>FOOBAR</bar></Foo>";
        fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooGood.class));
    }
    
    @Test
    public void testBad() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .registerSubtypes(FooBad.class)
                .build();
        String xml = "<Foo type=\"bad\" data=\"dummy\"><bar><bar>FOOBAR</bar></bar></Foo>";
        Foo fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooBad.class));

        xml = "<Foo data=\"dummy\" type=\"bad\"><bar><bar>FOOBAR</bar></bar></Foo>";
        fooRead = mapper.readValue(xml, Foo.class);
        assertThat(fooRead, instanceOf(FooBad.class));
    }
}
