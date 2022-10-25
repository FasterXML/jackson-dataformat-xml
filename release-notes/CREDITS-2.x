Here are people who have contributed to development of this project:
(version numbers in brackets indicate release in which the problem was fixed)

Tatu Saloranta, tatu.saloranta@iki.fi: author

Sebastien Dionne:

* Suggested Issue-23: Add @JacksonXmlText annotation (alias for JAXB @XmlValue),
  to support case of property values as 'unwrapped' text
 (2.0.1)

Pascal Gelinas:

* Reported and fixed #84: Problem with @JacksonXmlText when property output is suppressed
 (2.3.1)
* Reported and fixed #83: Add support for @JsonUnwrapped
 (2.4.0)
* Reported #97: Weird Exception during read with Type info
 (2.12.0)
 
Dan Jasek: (oillio@github)

* Contributed #126: Allow specifying properties that should be written as CData
 (2.5.0)

Leo Wang (wanglingsong@github)

* Reported #171: `@JacksonXmlRootElement` malfunction in multi-thread environment
 (2.6.4)

Yury Vasyutinskiy (Falland@github)

* Contributed #232: Implement `writeRawValue` in `ToXmlGenerator`
 (2.9.0)

Victor Khovanskiy (khovanskiy@githib)

* Reported #242: Deserialization of class inheritance depends on attributes order
 (2.10.0)

Nelson Dionisi (ndionisi@github)

* Reported #333: `OutputDecorator` not called with `XmlMapper`
 (2.10.0)

kevindaub@github.com:

* Reported, contributed fix for #336: WRITE_BIGDECIMAL_AS_PLAIN Not Used When Writing Pretty
 (2.10.0)

Sam Smith (Oracle Security Researcher)

* Reported #350: Wrap Xerces/Stax (JDK-bundled) exceptions during parser initialization
 (2.10.0)

Rohit Narayanan (rohitnarayanan@github)

* Reported #351: XmlBeanSerializer serializes AnyGetters field even with FilterExceptFilter
 (2.10.0)

Luke Korth (lkorth@github.com)

* Reported #366: XML containing xsi:nil is improperly parsed
 (2.10.2)

Martin Vysny (mvysny@github)

* Reported #395: Namespace repairing generates xmlns definitions for xml: prefix (which is implicit)
 (2.10.5)

James Bushell (jimnz111@github)

* Suggested #413: Null String field serialization through ToXmlGenerator causes NullPointerException
 (2.10.5)

Alexei Volkov (softkot@github)

* Reported #294: XML parser error with nested same element names
 (2.11.1)

Eric Schoonover (spoon16@github)

* Reported #86: Can not deserialize unwrapped list when `@JacksonXmlProperty` localName
  matches `@JacksonXmlRootElement` localName
 (2.12.0)

Denis Chernyshov (danblack@github)

* Reported #124: Deserialization if an empty list (with empty XML tag) results in `null`
 (2.12.0)

Julien Debon (Sir4ur0n@github)

* Reported #252: Empty (or self-closing) Element representing `List` is incorrectly
  deserialized as null, not Empty List
 (2.12.0)

Dave Jarvis (DaveJarvis@github)

* Requested #262: Make `ToXmlGenerator` non-final
 (2.12.0)

Joseph Petersen (jetersen@github)

* Reported #273: Input mismatch with case-insensitive properties
 (2.12.0)

Eduard Wirch (ewirch@github)

* Reported #314: Jackson gets confused by parent list element
 (2.12.0)

Jochen Schalanda (joschi@github)

* Reported #318: XMLMapper fails to deserialize null (POJO reference) from blank tag
 (2.12.0)

Migwel@github

* Contributed #360: Add a feature to support writing `xsi:nil` attribute for
  `null` values
 (2.12.0)
* Contributed fix for #445: `XmlMapper`/`UntypedObjectDeserializer` mixes
  multiple unwrapped collections
 (2.12.2)

Ingo Wiarda (dewarim@github)

* Reported #374: Deserialization fails with `XmlMapper` and
  `DeserializationFeature.UNWRAP_ROOT_VALUE`
 (2.12.0)

Ghenadii Batalski (ghenadiibatalski@github)

* Reported #377: `ToXmlGenerator` ignores `Base64Variant` while serializing `byte[]`
 (2.12.0)

David Schmidt (d-schmidt@github)

* Reported #390: Unexpected attribute at string fields causes extra objects to be
  created in parent list
 (2.12.0)

Akop Karapetyan (0xe1f@github)

* Reported #422: Elements containing <CDATA/> parsed incorrectly when at the end of another element
 (2.12.0)

Francesco Chicchiriccò (ilgrosso@github)

* Reported #435: After upgrade to 2.12.0, NPE when deserializing an empty element to `ArrayList`
 (2.12.1)

Westin Miller (westinrm@github)

* Contributed #456: Fix JsonAlias with unwrapped lists
 (2.12.3)

Tim Jacomb (timja@github)

* Reported #482: Use of non-Stax2-compatible Stax2 implementation fails when reading
 (2.12.4)

Daniel Kreck (daniel-kr@github)

* Reported #490: Problem when using defaultUseWrapper(false) in
  combination with polymorphic types
 (2.12.6)

Lennart Glauer (lglauer@github)

* Contributed fix for #490: Problem when using defaultUseWrapper(false) in
  combination with polymorphic types
 (2.12.6)

Fabian Meumertzheim (fmeum@github)

* Reported #463: NPE via boundary condition, document with only XML declaration
 (2.13.0)
* Reported #465: ArrayIndexOutOfBoundsException in UTF8Reader (ossfuzz)
 (2.13.0)

James Dudley (@dudleycodes)

* Reported #545: `@JacksonXmlText` does not work when paired with `@JsonRawValue`
 (2.14.0)

Jonas Konrad (@yawkat)

* Contributed fix for #545: `@JacksonXmlText` does not work when paired with `@JsonRawValue`
 (2.14.0)

Volkan Yazıcı (vy@github)

* Reported #491: `XmlMapper` 2.12 regression: no default no-arg ctor found
 (2.14.0)

Daniel Mensinger (mensinda@github)

* Contributed #531: Add mechanism for processing invalid XML names (transforming to
  valid ones)
 (2.14.0)

Eric Law (ericcwlaw@github)

* Reported #498: `XmlMapper` fails to parse XML array when the array only has one level
 (2.14.0)

David F. Elliott (dfelliott@github)

* Reported #550: Use of `ClassLoader`-taking `newFactory()` variant breaks applications
  using default JDK XML implementation
 (2.14.0)


