package tools.jackson.dataformat.xml.lists;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;

import tools.jackson.dataformat.xml.*;
import tools.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Combined tests for polymorphic list deserialization.
 * Merged from: PolymorphicList97Test, DeserializePolyList178Test,
 * PolymorphicList426Test, UnwrappedPolymorphicList490Test, PolymorphicList567Test
 */
public class PolymorphicListDeserTest extends XmlTestUtil
{
    // [dataformat-xml#97]
    @JsonTypeInfo(property = "type", use = Id.NAME)
    public static abstract class Foo97 {
        @JacksonXmlProperty(isAttribute = true)
        public String data;
    }

    @JsonTypeName("good")
    public static class FooGood97 extends Foo97 {
        public String bar;
    }

    @JsonTypeName("bad")
    public static class FooBad97 extends Foo97 {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> bar;
    }

    // [dataformat-xml#178]
    static class Company178 {
        public List<Computer178> computers;

        public Company178() {
            computers = new ArrayList<Computer178>();
        }

        public Company178 add(Computer178 computer) {
            if (computers == null) {
                computers = new ArrayList<Computer178>();
            }
            computers.add(computer);
            return this;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DesktopComputer178.class, name = "desktop"),
            @JsonSubTypes.Type(value = LaptopComputer178.class, name = "laptop")
    })
    static class Computer178 {
        public String id;
    }

    @JsonTypeName("desktop")
    static class DesktopComputer178 extends Computer178 {
        public String location;

        protected DesktopComputer178() { }
        public DesktopComputer178 with(String id0, String l) {
            id = id0;
            location = l;
            return this;
        }
    }

    @JsonTypeName("laptop")
    static class LaptopComputer178 extends Computer178 {
        public String vendor;
    }

    // [dataformat-xml#426]
    static class Auto426 {
        @JacksonXmlProperty(localName = "Object")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<CarParts426> carParts;
    }

    @JsonTypeIdResolver(CarPartsResolver426.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
    abstract static class CarParts426 {
        @JacksonXmlProperty(isAttribute = true)
        public String uid;

        @JacksonXmlProperty(localName = "Object")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<CarParts426> carParts;
    }

    static class Engine426 extends CarParts426{}
    static class Chassis426 extends CarParts426{}
    static class Motor426 extends CarParts426{}
    static class Body426 extends CarParts426{}

    static class CarPartsResolver426 extends TypeIdResolverBase {
        private static final long serialVersionUID = 1L;

        private JavaType superType;

        @Override
        public void init(JavaType javaType) {
            this.superType = javaType;
        }

        @Override
        public String idFromValue(DatabindContext context, Object o) {
            return idFromValueAndType(context, o, o.getClass());
        }

        @Override
        public String idFromValueAndType(DatabindContext context, Object o, Class<?> aClass) {
            return aClass.getSimpleName().replace("426", "");
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            Class<?> subType = null;
            switch (id) {
                case "Engine": subType = Engine426.class; break;
                case "Chassis": subType = Chassis426.class; break;
                case "Motor": subType = Motor426.class; break;
                case "Body": subType = Body426.class; break;
            }
            return context.constructSpecializedType(superType, subType);
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    // [dataformat-xml#490]
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MyType490.class, name = "myType"),
    })
    interface IMyType490 { }

    static class MyType490 implements IMyType490 {
        public final String stringValue;
        public final Collection<String> typeNames;

        @JsonCreator
        public MyType490(
                @JsonProperty("stringValue") String stringValue,
                @JsonProperty("typeNames") Collection<String> typeNames) {
            this.stringValue = stringValue;
            this.typeNames = typeNames;
        }
    }

    // [dataformat-xml#567]
    @JsonRootName("wrapper")
    static class Wrapper567 extends Base567 {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Item567> items = new ArrayList<>();

        public Wrapper567(List<Item567> items) {
            this.items = items;
        }

        public Wrapper567() {
        }

        public List<Item567> getItems() {
            return items;
        }

        public void setItems(List<Item567> items) {
            this.items = items;
        }
    }

    @JsonRootName("item")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Item567 {
        private String id;

        public Item567(String id) {
            this.id = id;
        }

        public Item567() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Wrapper567.class, name = "wrapper")
    })
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Base567 {
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#97]
    @Test
    public void testGood97() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .registerSubtypes(FooGood97.class)
                .build();
        String xml = "<Foo97 type=\"good\" data=\"dummy\"><bar>FOOBAR</bar></Foo97>";
        Foo97 fooRead = mapper.readValue(xml, Foo97.class);
        assertInstanceOf(FooGood97.class, fooRead);

        xml = "<Foo97 data=\"dummy\" type=\"good\" ><bar>FOOBAR</bar></Foo97>";
        fooRead = mapper.readValue(xml, Foo97.class);
        assertInstanceOf(FooGood97.class, fooRead);
    }

    // [dataformat-xml#97]
    @Test
    public void testBad97() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .registerSubtypes(FooBad97.class)
                .build();
        String xml = "<Foo97 type=\"bad\" data=\"dummy\"><bar><bar>FOOBAR</bar></bar></Foo97>";
        Foo97 fooRead = mapper.readValue(xml, Foo97.class);
        assertInstanceOf(FooBad97.class, fooRead);

        xml = "<Foo97 data=\"dummy\" type=\"bad\"><bar><bar>FOOBAR</bar></bar></Foo97>";
        fooRead = mapper.readValue(xml, Foo97.class);
        assertInstanceOf(FooBad97.class, fooRead);
    }

    // [dataformat-xml#178]
    @Test
    public void testPolyIdList178() throws Exception
    {
        Company178 input = new Company178();
        input.add(new DesktopComputer178().with("1", "http://foo.com"));
        final String LOC2 = "http://bar.com";
        input.add(new DesktopComputer178().with("2", LOC2));
        String xml = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);
        Company178 result = MAPPER.readValue(xml, Company178.class);
        assertNotNull(result.computers);
        assertEquals(2, result.computers.size());
        Computer178 comp = result.computers.get(1);
        assertNotNull(comp);
        assertEquals(DesktopComputer178.class, comp.getClass());
        DesktopComputer178 dt = (DesktopComputer178) comp;
        assertEquals(LOC2, dt.location);
    }

    // [dataformat-xml#426]
    @Test
    public void testPolymorphicList426() throws Exception
    {
        String xml = "" +
"<Auto426>\n" +
"  <Object uid='1' type='Engine'>\n" +
"    <Object uid='2' type='Chassis'></Object>\n" +
"    <Object uid='3' type='Motor'></Object>\n" +
"  </Object>\n" +
"  <Object uid='4' type='Body'></Object>\n" +
"</Auto426>";
        Auto426 result = MAPPER.readValue(xml, Auto426.class);
        assertNotNull(result);
        assertNotNull(result.carParts);
        assertEquals(2, result.carParts.size());
        CarParts426 cp = result.carParts.get(0);
        assertNotNull(cp);
        assertNotNull(cp.carParts);
        assertEquals(2, cp.carParts.size());
    }

    // [dataformat-xml#490]
    @Test
    public void testPolymorphicUnwrappedList490() throws Exception
    {
        XmlMapper xmlMapper = XmlMapper.builder()
                .defaultUseWrapper(false).build();

        List<String> typeNames = new ArrayList<>();
        typeNames.add("type1");
        typeNames.add("type2");
        MyType490 input = new MyType490("hello", typeNames);
        String doc = xmlMapper.writeValueAsString(input);
        IMyType490 result = xmlMapper.readValue(doc, IMyType490.class);

        assertNotNull(result);
        assertEquals(MyType490.class, result.getClass());
        MyType490 typedResult = (MyType490) result;
        assertEquals(Arrays.asList("type1", "type2"), typedResult.typeNames);
    }

    // [dataformat-xml#567]
    @Test
    public void testPolyList567_3items() throws Exception {
        String xmlString =
                "<?xml version='1.0' encoding='UTF-8'?>\n"
                +"<wrapper type='wrapper'>\n"
                +" <item><id>1</id></item>\n"
                +" <item><id>2</id></item>\n"
                +" <item><id>3</id></item>\n"
                +"</wrapper>\n"
                ;
        Base567 base = MAPPER.readValue(xmlString, Base567.class);
        assertEquals(3, ((Wrapper567)base).getItems().size());
    }

    // [dataformat-xml#567]
    @Test
    public void testPolyList567_roundtrip() throws Exception {
        Wrapper567 wrapper = new Wrapper567();
        Item567 item1 = new Item567("1");
        Item567 item2 = new Item567("2");
        wrapper.setItems(Arrays.asList(item1, item2));

        String writeValueAsString = MAPPER.writeValueAsString(wrapper);
        Base567 base = MAPPER.readValue(writeValueAsString, Base567.class);

        assertEquals(2, ((Wrapper567)base).getItems().size());
    }
}
