package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class ManualTest extends XmlTestBase
{

    @JacksonXmlRootElement(localName = "model")
    static class TestModel { public int a; }

    public static void main(String[] s) throws Exception {

        final XmlMapper xmlMapper = new XmlMapper();

        for (int i=0;i < 40; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(xmlMapper.writeValueAsString(new TestModel()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread.sleep(10000L);

    }
}
