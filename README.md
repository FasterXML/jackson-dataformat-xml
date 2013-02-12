# Overview

This projects contains [Jackson](http://http://wiki.fasterxml.com/JacksonHome) extension component for
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

Version 2.0 works for significant number of XML/JAXB use cases.
Missing functionality is tracked via [Issue tracker](./issues).

There are older versions (1.9.2)  available, which work with older Jackson versions (0.6.2): this is maintained on "1.9" branch,
and uses different group id ("com.fasterxml" as well as artifact id ("jackson-xml-databind")

Upcoming version 2.1 should have feature set that is very close to XML/JAXB use cases,
and specifically adds formerly missing support for "unwrapped Lists".

## Maven dependency

To use Jackson 2.x compatible version of this extension on Maven-based projects, use following dependency:

    <dependency>
      <groupId>com.fasterxml.jackson.dataformat</groupId>
      <artifactId>jackson-dataformat-xml</artifactId>
      <version>2.1.3</version>
    </dependency>

(or whatever version is most up-to-date at the moment)

Also: you usually also want to make sure that XML library in use is [Woodstox](http://wiki.fasterxml.com/WoodstoxHome) since it is not only faster than Stax implementation JDK provides, but also works better and avoids some known issues like adding unnecessary namespace prefixes.
You can do this by adding this in your `pom.xml`:

    <dependency>
      <groupId>org.codehaus.woodstox</groupId>
      <artifactId>woodstox-core-asl</artifactId>
      <version>4.1.4</version>
    </dependency>

# Usage

Although module implements low-level (`JsonFactory` / `JsonParser` / `JsonGenerator`) abstractions,
most usage is through data-binding level. This because a small number of work-arounds have been added
at data-binding level, to work around XML peculiarities: that is, stream of `JsonToken`s that parser
produces has idiosyncracies that need special handling.

Usually you either create `XmlMapper` simply by:

    XmlMapper mapper = new XmlMapper();

but in case you need to configure settings, you will want to do:

    JacksonXmlModule module = new JacksonXmlModule();
    // and then configure, for example:
    module.setDefaultUseWrapper(false);
    XmlMapper xmlMapper = new XmlMapper(module);
    // and you can also configure AnnotationIntrospectors etc here:

as many features that `XmlMapper` needs are provided by `JacksonXmlModule`; default
`XmlMapper` simply constructs module with default settings.

## Serializing POJOs as XML

Serialization is done very similar to JSON serialization: all that needs to change is `ObjectMapper` instance to use:

    // Important: create XmlMapper; it will use proper factories, workarounds
    ObjectMapper xmlMapper = new XmlMapper();
    String xml = xmlMapper.writeValue(new Simple());

and with POJO like:

    public class Simple {
        public int x = 1;
        public int y = 2;
    }

you would get something like:

    <Simple>
      <x>1</x>
      <y>2</y>
    </Simple>

(except that by default output is not indented: you can enabled indentation using standard Jackson mechanisms)

## Deserializing POJOs from XML

Similar to serialization, deserialization is not very different from JSON deserialization:

    ObjectMapper xmlMapper = new XmlMapper();
    Simple value = xmlMapper.readValue("<Simple><x>1</x><y>2</y></Simple>", Simple.class);

## Additional annotations

In addition to standard [Jackson annotations](jackson-annotations) and optional JAXB (`javax.xml.bind.annotation`), this project also adds couple of its own annotations for convenience, to support XML-specific details:

 * `@JacksonXmlElementWrapper` allows specifying XML element to use for wrapping `List` and `Map` properties
 * `@JacksonXmlProperty` allows specifying XML namespace and local name for a property; as well as whether property is to be written as an XML element or attribute.
 * `@JacksonXmlRootElement` allows specifying XML element to use for wrapping the root element (default uses 'simple name' of the value class)
 * `@JacksonXmlText` allows specifying that value of one property is to be serialized as "unwrapped" text, and not in an element.

for longer description, check out [XML module annotations](jackson-dataformat/wiki/JacksonXmlAnnotations).

## Known Limitations

Currently, following limitations exist beyond basic Jackson (JSON) limitations:

* Root value should be a POJO; and specifically following types can be serialized as properties but not as root values:
 * Java arrays
 * `java.util.Collection` values (Lists, Sets)
* Lists and arrays are "wrapped" by default, when using Jackson annotations, but unwrapped when using JAXB annotations (if supported, see below)
 * Unwrapped List/array support is added in Jackson 2.1 (2.0 does NOT support them; arrays are always wrapped)
 * `@JacksonXmlElementWrapper.useWrapping` can be set to 'false' to disable wrapping
 * `JacksonXmlModule.setDefaultUseWrapper()` can be used to specify whether "wrapped" or "unwrapped" setting is the default

# See Also

See [wiki page](jackson-dataformat-xml/wiki) for more information
