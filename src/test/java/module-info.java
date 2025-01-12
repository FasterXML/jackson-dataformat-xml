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
    //requires org.junit.jupiter.api;
    //requires org.junit.jupiter.params;

    requires com.ctc.wstx; // woodstox
    requires jakarta.xml.bind; // Jakarta-binding
    requires tools.jackson.module.jakarta.xmlbind;
    
    // Then exports same as main artifact

    exports tools.jackson.dataformat.xml;
    exports tools.jackson.dataformat.xml.annotation;
    exports tools.jackson.dataformat.xml.deser;
    exports tools.jackson.dataformat.xml.ser;
    exports tools.jackson.dataformat.xml.util;

    provides tools.jackson.core.TokenStreamFactory with
        tools.jackson.dataformat.xml.XmlFactory;
    provides tools.jackson.databind.ObjectMapper with
        tools.jackson.dataformat.xml.XmlMapper;
}
