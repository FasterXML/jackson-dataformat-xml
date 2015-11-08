# Overview

This projects contains [Jackson](http://wiki.fasterxml.com/JacksonHome) extension component for
reading and writing [XML](http://en.wikipedia.org/wiki/Xml) encoded data.

Further, the goal is to emulate how [JAXB](http://en.wikipedia.org/wiki/JAXB) data-binding works
with "Code-first" approach (that is, no support is added for "Schema-first" approach).
Support for JAXB annotations is provided by [JAXB annotation module](https://github.com/FasterXML/jackson-module-jaxb-annotations);
this module provides low-level abstractions (`JsonParser`, `JsonGenerator`, `JsonFactory`) as well as small number of higher level
overrides needed to make data-binding work.

It is worth noting, however, that the goal is NOT to be full JAXB clone; or to be general purpose XML toolkit.

Specifically:

 * While XML serialization should ideally be similar to JAXB output, deviations are not necessarily considered bugs -- we do "best-effort" matching
 * What should be guaranteed is that any XML written using this module must be readable using module as well: that is, we do aim for full XML serialization.
 * From above: there are XML constructs that module will not be able to handle; including some cases JAXB supports
 * This module may, however, also support constructs and use cases JAXB does not handle: specifically, rich type and object id support of Jackson are supported.

[![Build Status](https://fasterxml.ci.cloudbees.com/job/jackson-dataformat-xml-master/badge/icon)](https://fasterxml.ci.cloudbees.com/job/jackson-dataformat-xml-master/)

# Status

As of version 2.3, module is fully functional and considered production ready.

## Maven dependency

To use Jackson 2.x compatible version of this extension on Maven-based projects, use following dependency:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.dataformat</groupId>
  <artifactId>jackson-dataformat-xml</artifactId>
  <version>2.6.3</version>
</dependency>
```

(or whatever version is most up-to-date at the moment)

Also: you usually also want to make sure that XML library in use is [Woodstox](http://wiki.fasterxml.com/WoodstoxHome) since it is not only faster than Stax implementation JDK provides, but also works better and avoids some known issues like adding unnecessary namespace prefixes.
You can do this by adding this in your `pom.xml`:

```xml
<dependency>
  <groupId>org.codehaus.woodstox</groupId>
  <artifactId>woodstox-core-asl</artifactId>
  <version>4.4.1</version>
</dependency>
```

# Usage

Although module implements low-level (`JsonFactory` / `JsonParser` / `JsonGenerator`) abstractions,
most usage is through data-binding level. This because a small number of work-arounds have been added
at data-binding level, to work around XML peculiarities: that is, stream of `JsonToken`s that parser
produces has idiosyncracies that need special handling.

Usually you either create `XmlMapper` simply by:

```java
XmlMapper mapper = new XmlMapper();
```

but in case you need to configure settings, you will want to do:

```java
JacksonXmlModule module = new JacksonXmlModule();
// and then configure, for example:
module.setDefaultUseWrapper(false);
XmlMapper xmlMapper = new XmlMapper(module);
// and you can also configure AnnotationIntrospectors etc here:
```

as many features that `XmlMapper` needs are provided by `JacksonXmlModule`; default
`XmlMapper` simply constructs module with default settings.

## Android quirks

While usage on Android is the same as on standard JDKs, there is one thing that may cause issues:
since Google has chosen not to include whole JDK 1.6 API, `Stax` API (package `javax.xml.stream`) is missing.
This means that one has to add dependency explicitly. With Maven it can be done with

```xml
<dependency>
    <groupId>javax.xml.stream</groupId>
    <artifactId>stax-api</artifactId>
    <version>1.0-2</version>
    <scope>compile</scope>
</dependency>
```

or, if using other build tools, include similar dependency or download actual jar from

    http://repo1.maven.org/maven2/javax/xml/stream/stax-api/1.0-2/

## Serializing POJOs as XML

Serialization is done very similar to JSON serialization: all that needs to change is `ObjectMapper` instance to use:

```java
// Important: create XmlMapper; it will use proper factories, workarounds
ObjectMapper xmlMapper = new XmlMapper();
String xml = xmlMapper.writeValueAsString(new Simple());
// or
xmlMapper.writeValue(new File("/tmp/stuff.json"), new Simple());
```

and with POJO like:

```java
public class Simple {
    public int x = 1;
    public int y = 2;
}
```

you would get something like:

```xml
<Simple>
  <x>1</x>
  <y>2</y>
</Simple>
```

(except that by default output is not indented: you can enabled indentation using standard Jackson mechanisms)

## Deserializing POJOs from XML

Similar to serialization, deserialization is not very different from JSON deserialization:

```java
ObjectMapper xmlMapper = new XmlMapper();
Simple value = xmlMapper.readValue("<Simple><x>1</x><y>2</y></Simple>", Simple.class);
```

## Incremental/partial reading/writing (2.4+)

It is also possible to do incremental writes. This is done by creating Stax
`XMLInputFactory` separately (similar to how with JSON you would create `JsonGenerator`), and then:

```java
// First create Stax components we need
XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
StringWriter out = new StringWriter();
XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(out);

// then Jackson components
XmlMapper mapper = new XmlMapper(xmlInputFactory);

sw.writeStartDocument();
sw.writeStartElement("root");

// Write whatever content POJOs...
SomePojo value1 = ...;
OtherPojo value2 = ...;
mapper.writeValue(sw, value1);
mapper.writeValue(sw, value2);
// and/or regular Stax output
sw.writeComment("Some insightful commentary here");
sw.writeEndElement();
sw.writeEndDocument();
```

Similarly it is possible to read content, sub-tree by sub-tree; assuming similar XML content
we would use

```java
XMLOutputFactory f = XMLOutputFactory.newFactory();
File inputFile = ...;
XMLStreamReader sr = f.createXMLStreamReader(new FileInputStream(inputFile));

XmlMapper mapper = new XmlMapper();
sr.next(); // to point to <root>
sr.next(); // to point to root-element under root
SomePojo value1 = mapper.readValue(sr, SomePojo.class);
// sr now points to matching END_ELEMENT, so move forward
sr.next(); // should verify it's either closing root or new start, left as exercise
OtherPojo value = mapper.readValue(sr, OtherPojo.class);
// and more, as needed, then
sr.close();
```

## Additional annotations

In addition to standard [Jackson annotations](https://github.com/FasterXML/jackson-annotations) and optional JAXB (`javax.xml.bind.annotation`), this project also adds couple of its own annotations for convenience, to support XML-specific details:

 * `@JacksonXmlElementWrapper` allows specifying XML element to use for wrapping `List` and `Map` properties
 * `@JacksonXmlProperty` allows specifying XML namespace and local name for a property; as well as whether property is to be written as an XML element or attribute.
 * `@JacksonXmlRootElement` allows specifying XML element to use for wrapping the root element (default uses 'simple name' of the value class)
 * `@JacksonXmlText` allows specifying that value of one property is to be serialized as "unwrapped" text, and not in an element.
 * `@JacksonXmlCData` allows specifying that the value of a property is to be serialized within a CData tag.

for longer description, check out [XML module annotations](https://github.com/FasterXML/jackson-dataformat-xml/wiki/Jackson-XML-annotations).

## Known Limitations

Currently, following limitations exist beyond basic Jackson (JSON) limitations:

* Root value should be a POJO; and specifically following types can be serialized as properties but may not work as intended as root values
    * Primitive/Wrapper values (like `java.lang.Integer`)
    * `Enum`s
    * Java arrays
    * `java.util.Collection` values (Lists, Sets)
    * Note: over time some level of support has been added, and `Collection`s, for example, often work.
* Lists and arrays are "wrapped" by default, when using Jackson annotations, but unwrapped when using JAXB annotations (if supported, see below)
    * Unwrapped List/array support was added in Jackson 2.1 (2.0 does NOT support them; arrays are always wrapped)
    * `@JacksonXmlElementWrapper.useWrapping` can be set to 'false' to disable wrapping
    * `JacksonXmlModule.setDefaultUseWrapper()` can be used to specify whether "wrapped" or "unwrapped" setting is the default
* Tree Model is only supported in limited fashion: specifically, Java arrays and `Collection`s can be written, but can not be read, since it is not possible to distinguish Arrays and Objects without additional information.

# See Also

* XML module [wiki page](https://github.com/FasterXML/jackson-dataformat-xml/wiki) for more information
* Using XML with [DropWizard](https://github.com/dropwizard/dropwizard)? Check out [this extension](https://github.com/yunspace/dropwizard-xml)!
