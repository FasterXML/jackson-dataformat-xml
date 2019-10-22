Project: jackson-dataformat-xml

------------------------------------------------------------------------
= Releases
------------------------------------------------------------------------

2.11.0 (not yet released)

-

2.10.1 (not yet released)

- Upgrade Woodstox dependency to 6.0.2

2.10.0 (26-Sep-2019)

#242: Deserialization of class inheritance depends on attributes order
 (reported by Victor K)
#325: Problem with '$' in polymorphic type id names when "as class",
  "wrapper object", inner class
#326: Force namespace-repairing on `XMLOutputFactory` instances
#350: Wrap Xerces/Stax (JDK-bundled) exceptions during parser initialization
 (reported by Sam S)
#351: XmlBeanSerializer serializes AnyGetters field even with FilterExceptFilter
 (reported by Rohit N)
#354: Support mapping `xsi:nul` marked elements as `null`s (`JsonToken.VALUE_NULL`)

2.9.10 (21-Sep-2019)

#336: WRITE_BIGDECIMAL_AS_PLAIN Not Used When Writing Pretty
 (fix contributed by Kevin D)
#340: Incompatible woodstox-core and stax2-api dependencies (upgrade to
   `woodstox-core` 5.3.0)

2.9.9 (16-May-2019)

#333: `OutputDecorator` not called with `XmlMapper`
 (reported by Nelson D)
- Upgrade `woodstox-core` dependency from 5.0.3 to 5.1.0

2.9.8 (15-Dec-2018)

#270: Add support for `writeBinary()` with `InputStream` to `ToXMLGenerator`
 (requested by csbxvs@github; contributed by marc-christian-schulze@github)
#323: Replace slow string concatenation with faster `StringBuilder` (for
  long text content)

2.9.7 (19-Sep-2018)

No changes since 2.9.6

2.9.6 (12-Jun-2018)

#282: `@JacksonXmlRootElement` malfunction when using it with multiple `XmlMapper`s
  and disabling annotations
 (reported by benej60@github)

2.9.5 (26-Mar-2018)

No changes since 2.9.4

2.9.4 (24-Jan-2018)

- Changed the handling of String deserialization (with addition of `XmlStringDeserializer`)
  to work around a change in `jackson-databind`

2.9.3 (09-Dec-2017)
2.9.2 (14-Oct-2017)
2.9.1 (07-Sep-2017)

No changes since 2.9.0

2.9.0 (30-Jul-2017)

#162: XML Empty tag to Empty string in the object during xml deserialization
 (reported by thefalconfeat@github)
#232: Implement `writeRawValue` in `ToXmlGenerator`
 (contributed by Yury V)
#236: `ObjectReader.readValue()` throws unexpected exception `com.ctc.wstx.exc.WstxUnexpectedCharException'
 (reported by agyina@github)
#245: Default `DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT`
  to "enabled" for `XmlMapper`
#246: Add new feature, `FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL`
#250: Deserialization of `Exception` serialized as XML fails

2.8.9 (12-Jun-2017)

#228: `XmlReadContext` should hold current value
 (suggested by kfypmqqw@github)
#233: XmlMapper.copy() doesn't properly copy internal configurations
 (reported by gtrog@github)

2.8.8 (05-Apr-2017)
2.8.7 (21-Feb-2017)

#220: Avoid root-level wrapping for Map-like types, not just Maps
#222: DefaultXmlPrettyPrinter indentation configuration not preserved
 (reported by jtuc@github)

2.8.6 (12-Jan-2017)
2.8.5 (14-Nov-2016)

#213: `XmlSerializerProvider` does not use `withRootName` config for null
 (reported by gitlabbtr@github)
- Update woodstox dependency to 5.0.3

2.8.4 (14-Oct-2016)
2.8.3 (17-Sep-2016)
2.8.2 (30-Aug-2016)
2.8.1 (20-Jul-2016)

No changes since 2.8.0.

2.8.0 (04-Jul-2016)

#196: Mixed content not supported if there are child elements
 (reported by hvdp31@github)

2.7.10 (not yet released)

2.7.9 (04-Feb-2017)

No changes since 2.7.8

2.7.8 (26-Sep-2016)

#210: In `ToXmlGenerator` `WRITE_BIGDECIMAL_AS_PLAIN` is used the wrong way round
 (reported by xmluzr@github)
#211: Disable `SUPPORT_DTD` for `XMLInputFactory` unless explicitly overridden

2.7.7 (27-Aug-2016)

#204: FromXMLParser nextTextValue() incorrect for attributes
 (reported by frederikz@github)

2.7.6 (23-Jul-2016)
2.7.5 (11-Jun-2016)

No changes since 2.7.4

2.7.4 (29-Apr-2016)

#178: Problem with polymorphic serialization, inclusion type of
  `As.WRAPPER_OBJECT`, extra tag
#190: Ensure that defaults for `XMLInputFactory` have expansion of external
  parsed general entities disabled
#191: Strange behaviour of an empty item (but with whitespace between
  start/end tags) in List
 (reported by Hronom@github)

2.7.3 (16-Mar-2016)

No changes since 2.7.2

2.7.2 (27-Feb-2016)

- Change build to produce JDK6-compatible jar, to allow use on JDK6 runtime

2.7.1 (02-Feb-2016)

No changes since 2.7.0

2.7.0 (10-Jan-2016)

#156: Add `XmlMapper.setDefaultUseWrapper()` for convenience.
#167: Exception on deserializing empty element with an xsi attribute
#169: Fail to deserialize "empty" polymorphic classes
#180: Problem with deserialization of nested non-wrapped lists, with empty inner list

2.6.6 (05-Apr-2016)

No changes since 2.6.5.

2.6.5 (19-Jan-2016)

#177: Failure to deserialize unwrapped list where entry has empty content, attribute(s)

2.6.4 (07-Dec-2015)

#171: `@JacksonXmlRootElement` malfunction in multi-thread environment
 (reported by Leo W)
#172: XML INDENT_OUTPUT property fails to add newline/indent initial elements
 (reported by rpatrick00@github)

2.6.3 (12-Oct-2015)
2.6.2 (15-Sep-2015)
2.6.1 (09-Aug-2015)
2.6.0 (20-Jul-2015)

No changes since 2.5.

2.5.3 (24-Apr-2015)
2.5.2 (29-Mar-2015)

No changes since 2.5.1

2.5.1 (06-Feb-2015)

#133: Performance regression (2.4->2.5), 10% slower write via databind

2.5.0 (01-Jan-2015)

#120: Encoding not taken in account
 (reported by Sébastien D, sdeleuze@github)
#126: Allow specifying properties that should be written as CData
 (contributed by Dan J)
#129: Unwrapped collection containing empty CDATA not deserialized correctly
 (reported by tjconsult@github)

2.4.6 (not yet released)

- Improvement to `JsonParser.useDefaultPrettyPrinter()` override (wrt #136)

2.4.5 (13-Jan-2015)
2.4.4 (24-Nov-2014)
2.4.3 (04-Oct-2014)
2.4.2 (15-Aug-2014)

No changes.

2.4.1 (17-Jun-2014)

#117: @JsonAnyGetter + @JsonTypeInfo combination prevents serialization of properties as elements
 (reported by gawi@github)

2.4.0 (02-Jun-2014)

#76: UnrecognizedPropertyException when containing object is a Collection
 (reported by pierre@github)
#83: Add support for @JsonUnwrapped
 (contributed by Pascal G)
#99: Problem with DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Lists
 (reported by anton0xf@github)
#108: Unwrapped list ignore attributes if 2 lists in sequence
#111: Make vanilla `JaxbAnnotationIntrospector` work, without having to
 use `XmlJaxbAnnotationIntrospector`
#115: Allow incremental reading/writing with existing `XMLStreamReader`
 and `XMLStreamWriter`.
- Add `JsonFactory.canUseCharArrays()` introspection method

2.3.3 (10-Apr-2014)

#101: Invalid index error when deserializing unwrapped list element with multiple attributes
 (reported by yunspace@github; fix suggested by escholz@github)

2.3.2 (01-Mar-2014)

#81: Serialization of a polymorphic class As.Property with Identity info doesn't work 
 (fixed by Pascal G)
#91: @JsonPropertyOrder not working correctly with attributes
 (reported by thrykol@github)

2.3.1 (28-Dec-2013)

#84: Problem with @JacksonXmlText when property output is suppressed
 (contributed by Pascal G)

2.3.0 (14-Nov-2013)

#38: Support root-level Collection serialization
#64: Problems deserializing unwrapped lists, with optional (and missing) attribute.
#71: Fix issues with `XmlMapper.convertValue()`
- Add support for `JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN`
- Improved indentation

2.2.3 (22-Aug-2013):

No functional changes.

2.2.2 (31-May-2013)

#66: @JacksonXmlText should imply existence of property (without explicit marking) 
 (suggested by ShijunK@github)

2.2.1 (03-May-2013)

- Fixed problems with `XmlFactory.copy()` not calling underlying JsonFactory
  settings

2.2.0 (22-Apr-2013)

#47: First attribute of list value elements ignored
 (reported by dewthefifth@github)
#55: Problems deserializing wrapped lists (annotations not properly recognized)
#56: Indenting disables @JacksonXmlText
 (reported by edrik@github)

2.1.3 (19-Jan-2013)

* [JACKSON-879]: Missing OSGi import for 'com.fasterxml.jackson.databind.deser.std'
  causing error trying to load 'DelegatingDeserializer' base class.
 (reported by Martin S)
* [Issue#45]: Indentation should not be added if element only has attributes
* More fixes to [Issue#48], wrt thaw/unthaw

2.1.2 (04-Dec-2012)

* [Issue#42]: Problems with custom serializers, `JsonGenerator.writeObjectField`
 (reported by matejj@github)
* [Issue#44]: Problems with FilterProvider, serialization, annotations
 (reported by lalmeras@github)
* [Issue#46]: Indentation not working for unwrapped Lists
 (reported by lalmeras@github)
* [Issue#48]: 'XmlMapper.copy()' was missing copy of some fields in `XmlFactory`
 (reported by Sean B)

2.1.1 (12-Nov-2012)

* [Issue#39]: Improve error reporting when trying to use non-Stax2
  implementation, indentation
* [Issue#41]: Custom (de)serializer registration not working with
  JacksonXmlModule
 (reported by matejj@github)

2.1.0 (08-Oct-2012)

New minor version, with following changes:

* [Issue#6]: Add support for "unwrapped lists"; now unwrapped is also default
  when using JAXB annotations (but not with Jackson annotations, for backwards
  compatibility). @JacksonXmlElementWrapper allows explicit per-property
  overrides
* [Issue#30]: (from JAXB module, issue #11) Now `@XmlIDREF` forces use of
  id value for serialization of a reference property.
* [Issue#33]: Ignore attributes of elements for "List" objects
* [Issue#36]: Add 'JacksonXmlModule.setXMLTextElementName()' to allow
  matching 'value' text property of JAXB beans.

2.0.5 (27-Jul-2012)

* [Issue-29]: Binary value not cleared, leading to duplicated binary data
  for POJOs.
 (reported by 'farfalena'@git)

2.0.4 (26-Jun-2012)

no new fixes, dependencies to core components updated.

2.0.3 (15-Jun-2012)

* [Issue#26]: Root element should use 'default namespace'

2.0.2 (14-May-2012)

 No fixes, updates dependencies.

2.0.1 (14-Apr-2012)

* [Issue#23]: Add @JacksonXmlText annotation (alias for JAXB @XmlValue), to
  support case of text value with attributes
 (requested by Sebastian D)

2.0.0 (25-Mar-2012)

* [Issue#19]: Strange behavior with namespace generation when using 'isAttribute = true'
  (reported by Morten-Olav H)

[entries for version 1.x not retained)
