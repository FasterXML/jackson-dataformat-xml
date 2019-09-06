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
