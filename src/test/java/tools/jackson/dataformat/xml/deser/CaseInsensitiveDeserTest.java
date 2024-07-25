package tools.jackson.dataformat.xml.deser;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.*;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

// note: Copied from `jackson-databind`; related to [dataformat-xml#273]
public class CaseInsensitiveDeserTest extends XmlTestBase
{
    static class BaseResponse {
        public int errorCode;
        public String debugMessage;
    }

    static class InsensitiveCreator
    {
        int v;

        @JsonCreator
        public InsensitiveCreator(@JsonProperty("value") int v0) {
            v = v0;
        }
    }

    // [dataformat-xml#273]
    static class Depots273
    {
        public String command;
        public String taskId;

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Depot273> element;

        public void setElement(List<Depot273> l) {
//System.err.println("setElement: "+l);
            element = l;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Depot273
    {
        @JacksonXmlProperty(isAttribute = true)
        public String number;

        @JacksonXmlProperty(isAttribute = true)
        public String name;

        // Should not actually be necessary but unless unknown ignored is needed:
        @JacksonXmlText
        public String text;

        public void setNumber(String n) {
//System.err.println("SetNumber: '"+n+"'");
            number = n;
        }
        public void setName(String n) {
//System.err.println("setName: '"+n+"'");
            name = n;
        }
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    private final ObjectMapper INSENSITIVE_MAPPER = mapperBuilder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .build();

    public void testCaseInsensitiveBasic() throws Exception
    {
        final String DOC =
"<BaseResponse><ErrorCode>2</ErrorCode><DebugMessage>Signature not valid!</DebugMessage></BaseResponse>";

        // Ok with insensitive
        BaseResponse response = INSENSITIVE_MAPPER.readValue(DOC, BaseResponse.class);
        assertEquals(2, response.errorCode);
        assertEquals("Signature not valid!", response.debugMessage);

        // but not without
        try {
            MAPPER.readerFor(BaseResponse.class)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(DOC);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "ErrorCode");
        }
    }

    public void testCreatorWithInsensitive() throws Exception
    {
        final String DOC = "<root><VALUE>3</VALUE></root>";
        InsensitiveCreator bean = INSENSITIVE_MAPPER.readValue(DOC, InsensitiveCreator.class);
        assertEquals(3, bean.v);
    }

    // [dataformat-xml#273]
    public void testCaseInsensitiveComplex() throws Exception
    {
        final String DOC =
"<Depots273 Command='show depots' TaskId='1260'>\n"+
"  <Element Number='1' Name='accurev' Slice='1'\n"+
"exclusiveLocking='false' case='insensitive' locWidth='128'"+
"></Element>\n"+
"  <Element Number='2' Name='second accurev' Slice='2'\n"+
"exclusiveLocking='false' case='insensitive' locWidth='128'\n"+
"></Element>\n"+
"</Depots273>"
        ;

        Depots273 result = INSENSITIVE_MAPPER.readValue(DOC, Depots273.class);
        assertNotNull(result);
    }
}
