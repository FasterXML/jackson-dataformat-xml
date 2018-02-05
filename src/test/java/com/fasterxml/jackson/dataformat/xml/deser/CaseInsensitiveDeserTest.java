package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

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

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    private final ObjectMapper INSENSITIVE_MAPPER = XmlMapper.builder()
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
            MAPPER.readValue(DOC, BaseResponse.class);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "ErrorCode");
        }
    }

    public void testCreatorWithInsensitive() throws Exception
    {
        final String DOC = aposToQuotes("<root><VALUE>3</VALUE></root>");
        InsensitiveCreator bean = INSENSITIVE_MAPPER.readValue(DOC, InsensitiveCreator.class);
        assertEquals(3, bean.v);
    }
}
