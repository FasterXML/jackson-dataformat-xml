package com.fasterxml.jackson.dataformat.xml.misc;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequenceWrite493Test extends XmlTestUtil
{
    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testIssue493() throws Exception
    {
        try (Writer w = new StringWriter()) {
            SequenceWriter seqWriter = MAPPER.writer().writeValues(w);
        
            Map<String, String> reportObject = new HashMap<>();
            reportObject.put("a", "b");
            seqWriter.write(reportObject);

            seqWriter.close();

            assertEquals("<HashMap><a>b</a></HashMap>", w.toString().trim());
        }
    }
}
