// Generated 20-Mar-2019 using Moditect maven plugin
module com.fasterxml.jackson.dataformat.xml {
    requires java.xml;
    requires org.codehaus.stax2;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports com.fasterxml.jackson.dataformat.xml;
    exports com.fasterxml.jackson.dataformat.xml.annotation;
    exports com.fasterxml.jackson.dataformat.xml.deser;
    exports com.fasterxml.jackson.dataformat.xml.ser;
    exports com.fasterxml.jackson.dataformat.xml.util;

    provides com.fasterxml.jackson.core.JsonFactory with
        com.fasterxml.jackson.dataformat.xml.XmlFactory;
    provides com.fasterxml.jackson.core.ObjectCodec with
        com.fasterxml.jackson.dataformat.xml.XmlMapper;
}
