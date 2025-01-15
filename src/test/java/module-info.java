//  Jackson 3.x module-info for jackson-dataformat-xml Test artifact
module tools.jackson.dataformat.xml
{
    // First, same requires as the main artifact
    
    requires java.xml;
    requires org.codehaus.stax2;

    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    // Then test dependencies
    requires junit;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires com.ctc.wstx; // woodstox
    requires jakarta.xml.bind; // Jakarta-binding
    requires tools.jackson.module.jakarta.xmlbind;
    
    // Then same exports as main artifact, but as "opens"

    opens tools.jackson.dataformat.xml;
    opens tools.jackson.dataformat.xml.annotation;
    opens tools.jackson.dataformat.xml.deser;
    opens tools.jackson.dataformat.xml.ser;
    opens tools.jackson.dataformat.xml.util;

    // And then additional "opens" access for tests not in packages of main

    opens tools.jackson.dataformat.xml.adapters;
    opens tools.jackson.dataformat.xml.deser.builder;
    opens tools.jackson.dataformat.xml.deser.convert;
    opens tools.jackson.dataformat.xml.deser.creator;
    opens tools.jackson.dataformat.xml.dos;
    opens tools.jackson.dataformat.xml.failing;
    opens tools.jackson.dataformat.xml.failing.records;
    opens tools.jackson.dataformat.xml.fuzz;
    opens tools.jackson.dataformat.xml.jaxb;
    opens tools.jackson.dataformat.xml.incr;
    opens tools.jackson.dataformat.xml.jdk17;
    opens tools.jackson.dataformat.xml.lists;
    opens tools.jackson.dataformat.xml.misc;
    opens tools.jackson.dataformat.xml.node;
    opens tools.jackson.dataformat.xml.stream;
    opens tools.jackson.dataformat.xml.vld;
    opens tools.jackson.dataformat.xml.woodstox;
}
