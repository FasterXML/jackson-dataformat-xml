package com.fasterxml.jackson.dataformat.xml.deser;

import java.util.Set;

/**
 * Minimal API to be implemented by XML-backed parsers for which "virtual"
 * wrapping may be imposed.
 *<p>
 * NOTE: this method is considered part of internal implementation
 * interface, and it is <b>NOT</b> guaranteed to remain unchanged
 * between minor versions (it is however expected not to change in
 * patch versions). So if you have to use it, be prepared for
 * possible additional work.
 *
 * @since 2.15
 */
public interface ElementWrappable
{
    /**
     * Method that may be called to indicate that specified names
     * (only local parts retained currently: this may be changed in
     * future) should be considered "auto-wrapping", meaning that
     * they will be doubled to contain two opening elements, two
     * matching closing elements. This is needed for supporting
     * handling of so-called "unwrapped" array types, something
     * XML mappings like JAXB often use.
     */
    public void addVirtualWrapping(Set<String> namesToWrap0, boolean caseInsensitive);
}
