package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

public class CaseInsensitiveDeserTest extends XmlTestUtil
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

    @Test
    public void testCaseInsensitive1036() throws Exception
    {
        final String DOC =
"<BaseResponse><ErrorCode>2</ErrorCode><DebugMessage>Signature not valid!</DebugMessage></BaseResponse>";

        // Ok with insensitive
        BaseResponse response = INSENSITIVE_MAPPER.readValue(DOC, BaseResponse.class);
        assertEquals(2, response.errorCode);
        assertEquals("Signature not valid!", response.debugMessage);

        // but not without
        try {
            MAPPER.readValue(DOC, BaseResponse.class);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "ErrorCode");
        }
    }

    // [dataformat-xml#273]
    @Test
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
