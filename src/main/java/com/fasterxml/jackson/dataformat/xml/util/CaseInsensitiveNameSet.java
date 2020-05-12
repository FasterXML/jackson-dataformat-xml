package com.fasterxml.jackson.dataformat.xml.util;

import java.util.*;

/**
 * Helper class for matching element wrappers, possibly in case-insensitive
 * manner.
 *
 * @since 2.12
 */
public final class CaseInsensitiveNameSet extends AbstractSet<String>
{
    private final Set<String> _namesToMatch;

    private CaseInsensitiveNameSet(Set<String> namesToMatch) {
        _namesToMatch = namesToMatch;
    }

    public static CaseInsensitiveNameSet construct(Set<String> names0) {
        Set<String> namesToMatch = new HashSet<String>(names0);
        for (String name : names0) {
            namesToMatch.add(name.toLowerCase());
        }
        return new CaseInsensitiveNameSet(namesToMatch);
    }

    @Override
    public boolean contains(Object key0) {
        final String key = (String) key0;
        if (_namesToMatch.contains(key)) {
            return true;
        }
        final String lc = key.toLowerCase();
        return (lc != key) && _namesToMatch.contains(lc);
    }

    @Override
    public Iterator<String> iterator() {
        return _namesToMatch.iterator();
    }

    @Override
    public int size() {
        return _namesToMatch.size();
    }
}
