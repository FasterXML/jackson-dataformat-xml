# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Jackson XML dataformat module extends Jackson to serialize/deserialize XML using the STAX (Streaming API for XML) processing engine. The goal is to emulate JAXB "code-first" data binding while leveraging Jackson's rich type system and features. This is NOT a full JAXB clone or general-purpose XML toolkit.

**Key Principle:** Any XML written by this module MUST be readable by this module (guaranteed round-trip support).

**This branch is Jackson 3.x.** That means:

- Java package root is `tools.jackson.dataformat.xml` (NOT `com.fasterxml.jackson.dataformat.xml`).
  Only the `com.fasterxml.jackson.annotation` annotations package keeps the old root.
- Maven coordinates are `tools.jackson.dataformat:jackson-dataformat-xml`, parent
  `tools.jackson:jackson-base`.
- Mappers are **immutable**: configure via `XmlMapper.builder()`, never via
  `mapper.enable(...)` / `mapper.disable(...)` (those no longer exist).
- Format features moved out of the parser/generator classes into top-level enums
  `XmlReadFeature` and `XmlWriteFeature`, and several defaults changed (see below).
- `JacksonXmlModule` is now `XmlModule`.
- The module is a real JPMS module (`src/main/java/module-info.java`).

## Build & Test Commands

This is a Maven-based project. Use the Maven wrapper (`./mvnw`) for consistent builds.

### Basic Commands

```bash
# Clean and build
./mvnw clean install

# Run all tests
./mvnw test

# Run tests without building
./mvnw surefire:test

# Run a single test class
./mvnw test -Dtest=XmlMapperTest

# Run a single test method
./mvnw test -Dtest=XmlMapperTest#testSimpleSerialization

# Build without running tests
./mvnw clean install -DskipTests

# Generate code coverage report (JaCoCo)
./mvnw clean test
# Report available at: target/site/jacoco/index.html
```

### JDK Baseline

- Java **17** is the baseline for both main and test sources (inherited from
  `jackson-base`); CI builds on JDK 17, 21 and 24.
- Unlike 2.x, there is **no** separate `src/test-jdk17` source root and no JDK profile:
  Records and other JDK 17+ tests live in the regular test tree
  (`.../xml/records/`, `.../xml/jdk17/`, `.../xml/tofix/records/`).

### Working with Test Files

- Test sources: `src/test/java/tools/jackson/dataformat/xml/`
- **Test base class: `XmlTestUtil`** — extend this for new tests (JUnit 5 based;
  provides `newMapper()`, `mapperBuilder()`, shared POJOs, and assertion helpers).
  All test classes use it.
- `XmlTestBase` (the deprecated JUnit 3/4 base from 2.x) has been **removed**.
- Tests use JUnit 5 (`org.junit.jupiter.api.Test`, static `Assertions` imports) only;
  JUnit 4 is no longer on the test classpath.

## Repository Branch Structure

- `3.x` — active development branch for the next 3.x minor (currently `3.3.0-SNAPSHOT`)
- `3.2` — maintenance branch for `3.2.x` patch releases
- `3.1` — maintenance branch for `3.1.x` patch releases
- `3.0` — maintenance branch for `3.0.x` patch releases
- `2.x` — 2.x development branch (currently `2.23.0-SNAPSHOT`); `2.22` etc. are its
  maintenance branches

Fixes flow upward: patch branch → next minor → `3.x`. Target a PR at the oldest branch
the fix should ship in.

Version numbers are inherited from the `tools.jackson:jackson-base` parent POM.

## High-Level Architecture

### Core Components Flow

```
User Code
    ↓
XmlMapper (extends ObjectMapper) ← Primary API entry point
    ↓
XmlFactory (extends TextualTSFactory, a TokenStreamFactory) ← Creates parsers/generators
    ├→ FromXmlParser (XML → JSON token stream)
    │    ├─ XmlTokenStream (STAX abstraction layer)
    │    └─ XMLStreamReader (Woodstox)
    │
    └→ ToXmlGenerator (JSON token stream → XML)
         └─ XMLStreamWriter (Woodstox)
```

### Builder-Style Construction

Mapper and factory are configured **exclusively** through builders (3.x mappers are
immutable after construction):

```java
XmlMapper mapper = XmlMapper.builder()
    .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
    .defaultUseWrapper(false)
    .build();
```

`XmlMapper.Builder` extends `MapperBuilder` and adds `defaultUseWrapper(boolean)`,
`nameForTextElement(String)`, and `enable`/`disable`/`configure` overloads for
`XmlReadFeature` / `XmlWriteFeature`. Builder state is saved in `XmlBuilderState`
(a `MapperBuilderState`) so `rebuild()` / JDK serialization round-trip correctly.

`XmlFactoryBuilder` configures the `XMLInputFactory`/`XMLOutputFactory`, the STAX
`ClassLoader`, the `XmlNameProcessor`, `nameForTextElement`, and format features.

Other entry points:

- `XmlMapper.shared()` — globally shared default-configured instance (handy for
  untyped/tree-model work). Do not mutate expectations around it; it is immutable.
- `XmlMapper.builderWithJackson2Defaults()` / `XmlMapper.Builder.configureForJackson2()`
  and `XmlFactory.builderWithJackson2Defaults()` — start from settings closer to
  Jackson 2.x defaults. Still a work in progress; it does not replicate 2.x exactly.

### Module Registration System

`XmlModule` (a `JacksonModule`) centralizes XML-specific configuration and is
registered automatically by `XmlMapper.Builder`:

- Registers `XmlBeanSerializerModifier` — hooks into serializer creation
- Registers `XmlBeanDeserializerModifier` — hooks into deserializer creation
  (receives the builder's `nameForTextElement`)

The `JacksonXmlAnnotationIntrospector` and the XML-specific
`SerializationContexts` / `DeserializationContexts` are wired in by
`XmlMapper.Builder` itself, not by the module.

### Serialization Pipeline (Object → XML)

```
Object
  → XmlMapper.writeValue()
  → ToXmlGenerator (implements JsonGenerator)
  → XmlBeanSerializerModifier (customizes bean serializers)
  → XmlBeanSerializer + XmlBeanPropertyWriter
     ├─ Determines element vs attribute
     ├─ Handles namespace mappings
     └─ Manages wrapper elements for collections
  → XmlSerializationContext (handles root element naming)
  → XMLStreamWriter (Woodstox STAX)
  → XML Output
```

**Key Classes:**
- `XmlBeanSerializerBase` / `XmlBeanSerializer` - Extend Jackson's `BeanSerializer` with XML-specific logic
- `XmlBeanPropertyWriter` - Determines if property is element or attribute
- `UnwrappingXmlBeanSerializer` - Handles `@JsonUnwrapped` properties
- `XmlSerializationContext` / `XmlSerializationContexts` - XML-specific
  `SerializationContextExt` (replaces 2.x `XmlSerializerProvider`); root-name handling
- `XmlRootNameLookup` - Caches root element name calculations
- `DefaultXmlPrettyPrinter` (implements `XmlPrettyPrinter`) - XML-aware formatting

### Deserialization Pipeline (XML → Object)

```
XML Input
  → XMLStreamReader (STAX/Woodstox)
  → FromXmlParser (implements JsonParser)
  → XmlTokenStream (converts XML events to JSON tokens)
     ├─ START_ELEMENT → START_OBJECT
     ├─ Attributes → PROPERTY_NAME + VALUE pairs
     ├─ Text content → VALUE_STRING (property name = "")
     └─ END_ELEMENT → END_OBJECT
  → XmlBeanDeserializerModifier (customizes deserializers)
     ├─ Renames wrapper properties
     ├─ Handles @JacksonXmlText
     └─ Manages collection wrapping/unwrapping
  → Standard Jackson Deserializers
  → Object
```

**Key Classes:**
- `FromXmlParser` - Wraps STAX reader, exposes JSON-like token stream
- `XmlTokenStream` - Intermediate abstraction with token replay/lookahead
- `WrapperHandlingDeserializer` - Handles wrapped/unwrapped collections
- `ElementWrapper` / `ElementWrappable` - Wrapper-name metadata plumbing
- `XmlReadContext` - Tracks parsing context (current element, namespace)
- `XmlDeserializationContext` / `XmlDeserializationContexts` - XML-specific
  `DeserializationContextExt`
- `XmlTextDeserializer` - Deserializes element text content

### Collection Wrapping Pattern

A critical XML-specific concern is how collections are represented:

**Wrapped (default behavior):**
```xml
<items>
  <item>A</item>
  <item>B</item>
</items>
```

**Unwrapped:**
```xml
<item>A</item>
<item>B</item>
```

Control via:
- `@JacksonXmlElementWrapper(useWrapping = false)` per property
- `XmlMapper.builder().defaultUseWrapper(false)` globally
  (there is no `XmlModule.setDefaultUseWrapper()` in 3.x)
- `WrapperHandlingDeserializer` implements the complex logic

### Annotation System

**Jackson XML Annotations** (`tools.jackson.dataformat.xml.annotation`):
- `@JacksonXmlProperty(isAttribute=true)` - Mark property as XML attribute
- `@JacksonXmlElementWrapper` - Control collection wrapper elements
- `@JacksonXmlText` - Property represents element text content (not a child element)
- `@JacksonXmlCData` - Wrap value in CDATA section
- `@JacksonXmlRootElement` - **Deprecated since 3.0**; use
  `com.fasterxml.jackson.annotation.@JsonRootName` instead

Standard Jackson annotations still come from `com.fasterxml.jackson.annotation`.

**JAXB Support:**
The `jaxb/` main-source package (and `XmlJaxbAnnotationIntrospector`) is **gone** in 3.x.
JAXB/Jakarta annotation support comes from the separate
`jackson-module-jakarta-xmlbind-annotations` module — use its
`JakartaXmlBindAnnotationIntrospector` directly. Here it is a **test-scope** dependency
only (see `XmlTestUtil.jakartaXMLBindAnnotationIntrospector()` and the `jaxb/` tests).

**Polymorphic Type Handling:**
`XmlTypeResolverProvider`, `XmlTypeResolverBuilder` and `DefaultingXmlTypeResolverBuilder`
adapt Jackson's type resolution (`@JsonTypeInfo`, default typing) to XML — notably making
`As.PROPERTY` inclusion work as an XML attribute.

### STAX Integration

**Woodstox** is the preferred STAX implementation (faster and more reliable than JDK's default).

**Factory Configuration:**
- `XmlFactory` manages `XMLInputFactory` and `XMLOutputFactory`
- `XmlFactoryBuilder.defaultXmlInputFactory()` sets the security defaults:
  `IS_SUPPORTING_EXTERNAL_ENTITIES = false`, `SUPPORT_DTD = false`
- `IS_REPAIRING_NAMESPACES = true` for automatic namespace handling
- `IS_COALESCING = true` to simplify text content processing
- `Stax2JacksonReaderAdapter` bridges non-Woodstox (Stax2-less) readers

**Custom STAX Configuration Example:**
```java
XMLInputFactory ifactory = new WstxInputFactory();
ifactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, 32000);
XmlFactory xf = XmlFactory.builder()
    .xmlInputFactory(ifactory)
    .build();
XmlMapper mapper = XmlMapper.builder(xf).build();
```

Note: supplying your own `XMLInputFactory` also means supplying your own security
settings — the XXE/DTD defaults above are only applied to the factory-created default.

### Name Processing

`XmlNameProcessor` allows custom XML name transformations, configured via
`XmlFactoryBuilder.xmlNameProcessor(...)` and applied during both serialization and
deserialization. `XmlNameProcessors` provides ready-made implementations:
`newPassthroughProcessor()` (default), `newReplacementProcessor()`,
`newBase64Processor()`, and `newAlwaysOnBase64Processor()` — the latter two encode
names that are not valid XML names.

### Feature Flags

In 3.x the nested `FromXmlParser.Feature` / `ToXmlGenerator.Feature` enums are replaced
by top-level `tools.jackson.dataformat.xml.XmlReadFeature` and `XmlWriteFeature`.
**Several defaults flipped to enabled compared to 2.x** — check the default when
writing tests.

**`XmlReadFeature`** (deserialization) — complete set, with 3.x defaults:
- `AUTO_DETECT_XSI_TYPE` (default **on**) - Process `xsi:type` attributes for polymorphism
- `EMPTY_ELEMENT_AS_NULL` (default off) - Treat `<element/>` as null
- `PROCESS_XSI_NIL` (default **on**) - Honor `xsi:nil="true"`

**`XmlWriteFeature`** (serialization) — complete set, with 3.x defaults:
- `WRITE_XML_DECLARATION` (default off) - Output `<?xml version="1.0"?>`
- `WRITE_XML_1_1` (default off) - Use XML 1.1
- `WRITE_STANDALONE_YES_TO_XML_DECLARATION` (default off) - Add `standalone="yes"`
  (needs `WRITE_XML_DECLARATION`)
- `WRITE_NULLS_AS_XSI_NIL` (default **on**) - Add `xsi:nil="true"` for null values
- `UNWRAP_ROOT_OBJECT_NODE` (default **on**) - For a single-entry root `ObjectNode`,
  use its key as root element name
- `AUTO_DETECT_XSI_TYPE` (default **on**) - Add `xsi:type` for polymorphic types
- `WRITE_XML_SCHEMA_CONFORMING_FLOATS` (default **on**) - Emit `INF`/`-INF`/`NaN`
  per XML Schema

Configure via `XmlMapper.builder().enable(...)/.disable(...)`, per-call via
`ObjectReader.with(...)` / `ObjectWriter.with(...)`, or on `XmlFactory.builder()`.
`configureForJackson2()` turns off the five features whose defaults changed
(`WRITE_NULLS_AS_XSI_NIL`, `UNWRAP_ROOT_OBJECT_NODE`, `XmlWriteFeature.AUTO_DETECT_XSI_TYPE`,
`WRITE_XML_SCHEMA_CONFORMING_FLOATS`, `XmlReadFeature.AUTO_DETECT_XSI_TYPE`).

## Source Code Structure

```
src/main/java/
├── module-info.java            - JPMS module `tools.jackson.dataformat.xml`
└── tools/jackson/dataformat/xml/
    ├── XmlMapper.java              - Primary API, extends ObjectMapper (has Builder)
    ├── XmlFactory.java             - Creates parsers/generators
    ├── XmlFactoryBuilder.java      - Builder for XmlFactory (STAX factories, name processor)
    ├── XmlModule.java              - Module for XML configuration (was JacksonXmlModule)
    ├── XmlReadFeature.java / XmlWriteFeature.java   - Format feature enums
    ├── JacksonXmlAnnotationIntrospector.java / XmlAnnotationIntrospector.java
    ├── XmlTypeResolverProvider.java / XmlTypeResolverBuilder.java
    │   / DefaultingXmlTypeResolverBuilder.java
    ├── XmlNameProcessor.java / XmlNameProcessors.java  - XML name transformation
    ├── XmlPrettyPrinter.java
    ├── annotation/                 - @JacksonXml* annotations
    ├── deser/                      - Deserialization (XML → Object)
    │   ├── FromXmlParser.java      - Parser implementation
    │   ├── XmlTokenStream.java     - STAX abstraction layer
    │   ├── XmlBeanDeserializerModifier.java
    │   ├── WrapperHandlingDeserializer.java
    │   ├── ElementWrapper.java / ElementWrappable.java
    │   ├── XmlDeserializationContext.java / XmlDeserializationContexts.java
    │   ├── XmlReadContext.java
    │   └── XmlTextDeserializer.java
    ├── ser/                        - Serialization (Object → XML)
    │   ├── ToXmlGenerator.java     - Generator implementation
    │   ├── XmlBeanSerializer.java / XmlBeanSerializerBase.java
    │   ├── UnwrappingXmlBeanSerializer.java
    │   ├── XmlBeanPropertyWriter.java
    │   ├── XmlBeanSerializerModifier.java
    │   └── XmlSerializationContext.java / XmlSerializationContexts.java
    └── util/                       - Utilities
        ├── StaxUtil.java                  - STAX exception handling
        ├── XmlRootNameLookup.java         - Root element naming
        ├── DefaultXmlPrettyPrinter.java
        ├── Stax2JacksonReaderAdapter.java
        ├── AnnotationUtil.java / TypeUtil.java / XmlInfo.java
        └── CaseInsensitiveNameSet.java

src/test/java/tools/jackson/dataformat/xml/
├── deser/                      - Deserialization tests (+ builder/, convert/, creator/)
├── ser/                        - Serialization tests
├── stream/                     - Low-level parser/generator tests
├── lists/                      - Collection handling tests
├── node/                       - Tree model (JsonNode) tests
├── misc/                       - Assorted feature tests
├── dos/                        - Denial-of-service / resource-limit tests
├── records/, jdk17/            - Java Records and other JDK 17+ tests
├── adapters/, incr/, jaxb/, vld/, woodstox/
├── fuzz/                       - Regression tests from OSS-Fuzz findings
├── tofix/                      - Tests for known issues (+ records/; see below)
├── testutil/                   - Test helpers (+ failure/ annotations)
└── XmlTestUtil.java            - Base test class for all tests
```

Note: `module-info.java` must be updated whenever a package is added, removed, or newly
needs to be exported.

## Test Organization

- Tests are organized by feature area (deser, ser, lists, stream, node, etc.)
- Issue-specific tests named like `RootName374Test.java` (GitHub issue #374)
- `tofix/` holds tests for known limitations/bugs. These extend `XmlTestUtil` and are
  annotated `@JacksonTestFailureExpected` (see `testutil/failure/`), which inverts the
  result: the test *fails* the build if it unexpectedly starts passing. When you fix a
  bug, remove the annotation and move the test into the proper package.
- `fuzz/` holds regression tests for OSS-Fuzz reports; `dos/` holds denial-of-service /
  resource-limit tests.

## Known Limitations

- **Tree Model** (`JsonNode`): Does not perfectly match XML infoset
  - Mixed content (text + elements) not fully supported
  - Repeated elements handled specially (see #403)
- **Root Values**: Work best with POJOs; primitives, Strings, Collections have limitations
- **Collection Wrapping**: Default behavior differs between Jackson and JAXB annotations
- **Namespace URIs**: Not verified during deserialization (only local names matched)
- **Polymorphic Types**: Some inclusion mechanisms unsupported (e.g., `WRAPPER_ARRAY`)

See README.md "Known Limitations" section for complete list.

## Development Notes

### Release Notes
Every user-visible change gets an entry in `release-notes/VERSION` (issue number,
one-line description, credit line) and, for external contributors, `release-notes/CREDITS`.
The `-2.x` suffixed files are the frozen 2.x history — do not add 3.x entries there.

### Security
- External entity expansion disabled by default (XXE prevention)
- DTD processing disabled
- Both are configured in `XmlFactoryBuilder.defaultXmlInputFactory(...)`; a
  caller-supplied `XMLInputFactory` bypasses them.

### Streaming vs Databinding
- Low-level streaming (direct `FromXmlParser`/`ToXmlGenerator` use) is possible but not primary use case
- Databinding layer includes necessary XML-specific workarounds
- For incremental/partial reading/writing, combine STAX APIs with `XmlMapper` (see `incr/` tests)

### Modifying Serializers/Deserializers
Use the Modifier pattern:
- Extend `XmlBeanSerializerModifier` for serialization customization
- Extend `XmlBeanDeserializerModifier` for deserialization customization
- Register via `XmlModule` or a custom `JacksonModule`

### Adding New Features
1. Consider if it's a parser/generator feature (token stream level) or serializer/deserializer feature (databinding level)
2. Parser/Generator: Modify `FromXmlParser`/`ToXmlGenerator` and potentially `XmlTokenStream`
3. Databinding: Use Modifier pattern or custom `ValueSerializer`/`ValueDeserializer`
4. Add a feature flag to `XmlReadFeature`/`XmlWriteFeature` if it's optional behavior.
   Within a 3.x minor, defaults must stay backwards-compatible; default changes belong
   on `3.x`, and any newly-enabled-by-default write/read feature should also be
   disabled in `configureForJackson2()`.
5. Add tests in the appropriate subdirectory, extending `XmlTestUtil`
6. Add a release-notes entry

## Dependencies

**Core (compile):**
- `jackson-core`, `jackson-databind` (`tools.jackson.core` group), `jackson-annotations`
  (still `com.fasterxml.jackson.core` group) — versions from the parent BOM
- `stax2-api` (enhanced STAX API)
- `woodstox-core` (STAX implementation)
- `stax-api` (`provided` scope only, for old JDK compatibility)

**Test:**
- JUnit 5 (`junit-jupiter`, `junit-jupiter-api`)
- `jackson-module-jakarta-xmlbind-annotations` + `jakarta.xml.bind-api` (JAXB interop testing)
- `sjsxp` (alternative STAX impl for testing)

**Version Management:**
Versions inherited from the `tools.jackson:jackson-base` parent POM, which manages the
Jackson BOM (bill of materials).
