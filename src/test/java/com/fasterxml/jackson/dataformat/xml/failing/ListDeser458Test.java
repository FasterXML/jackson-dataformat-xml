package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class ListDeser458Test extends XmlTestBase
{
    static class Channel458 {
        public String channelId;
    }

    static class ChannelSet458 {
        public String setId;

        // is default but just for readability
        @JacksonXmlElementWrapper(useWrapping = true)
        public List<Channel458> channels;
    }

    private final ObjectMapper XML_MAPPER = newMapper();

    public void testIssue458() throws Exception
    {
        String input = "<ChannelSet458>\n" +
                "<setId>2</setId>\n" +
                "<channels>\n" +
                "</channels>\n" +
                "</ChannelSet458>";
        ChannelSet458 inputProxyChannelStatus = XML_MAPPER.readValue(input, ChannelSet458.class);
        assertEquals("List should be empty", 0,
                inputProxyChannelStatus.channels.size());
    }
}
