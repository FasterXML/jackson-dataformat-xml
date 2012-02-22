# Overview

This projects aims at adding [Jackson](http://http://wiki.fasterxml.com/JacksonHome) extension component to allow
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
 
# Status

Version 2.0.0-RC1 works for significant number of XML/JAXB use cases.
Missing functionality is tracked via [Issue tracker](./issues).

There are older versions (1.9.2)  available, which work with older Jackson versions (0.6.2): this is maintained on "1.9" branch,
and uses different group id ("com.fasterxml" as well as artifact id ("jackson-xml-databind")

## Maven dependency

To use Jackson 2.x compatible version of this extension on Maven-based projects, use following dependency:

    <dependency>
      <groupId>com.fasterxml.jackson.dataformat</groupId>
      <artifactId>jackson-dataformat-xml</artifactId>
      <version>2.0.0-RC1</version>
    </dependency>

(or whatever version is most up-to-date at the moment)

# Usage

Although module implements low-level (`JsonFactory` / `JsonParser` / `JsonGenerator`) abstractions,
most usage is through data-binding level. This because a small number of work-arounds have been added
at data-binding level, to work around XML peculiarities: that is, stream of `JsonToken`s that parser
produces has idiosyncracies that need special handling.

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

# See Also

See [wiki page](./wiki) for more information
