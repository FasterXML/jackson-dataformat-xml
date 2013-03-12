package com.fasterxml.jackson.dataformat.xml;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class TestSerializationManual extends XmlTestBase
{
    public static class Value {
        public int num;

        public Value(int n) { num = n; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    public void testIssue54() throws Exception
    {
        XmlMapper xmlMapper = new XmlMapper();
        StringWriter sw = new StringWriter();
        ToXmlGenerator generator = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(sw);
//        generator.initGenerator();

        generator.setNextName(new QName("items"));
        generator.writeStartObject();
        ArrayList<Value> values = new ArrayList<Value>();
        values.add(new Value(13));
        values.add(new Value(456));
        for (Value value : values) {
            generator.writeFieldName("foo");
            generator.setNextName(new QName("item"));
            generator.writeObject(value);
        }
        generator.writeEndObject();
        generator.close();
        
        String xml = sw.toString();
        assertEquals("<items><item><num>13</num></item><item><num>456</num></item></items>", xml);
   }
}
