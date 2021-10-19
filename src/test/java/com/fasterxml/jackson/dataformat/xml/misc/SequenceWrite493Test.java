package com.fasterxml.jackson.dataformat.xml.misc;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class SequenceWrite493Test extends XmlTestBase
{
    private final XmlMapper MAPPER = newMapper();

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
