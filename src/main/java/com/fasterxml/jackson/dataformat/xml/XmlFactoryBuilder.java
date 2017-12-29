package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.base.DecorableTSFactory.DecorableTSFBuilder;

/**
 * {@link com.fasterxml.jackson.core.TokenStreamFactory.TSFBuilder}
 * implementation for constructing {@link XmlFactory}
 * instances.
 *
 * @since 3.0
 */
public class XmlFactoryBuilder extends DecorableTSFBuilder<XmlFactory, XmlFactoryBuilder>
{
    public XmlFactoryBuilder() {
        super();
    }

    public XmlFactoryBuilder(XmlFactory base) {
        super(base);
    }

    @Override
    public XmlFactory build() {
        // 28-Dec-2017, tatu: No special settings beyond base class ones, so:
        return new XmlFactory(this);
    }
}
