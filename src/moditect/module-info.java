module tools.jackson.dataformat.xml {
    requires java.xml;
    requires org.codehaus.stax2;

    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

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
