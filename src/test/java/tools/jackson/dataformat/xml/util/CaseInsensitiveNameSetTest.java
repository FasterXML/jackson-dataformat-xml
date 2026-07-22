package tools.jackson.dataformat.xml.util;

import java.util.Collections;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import tools.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class CaseInsensitiveNameSetTest extends XmlTestUtil
{
    // Names with 'I'/'i' fold differently under the Turkish locale
    // ("I".toLowerCase() -> dotless "ı"), so a set built with the default
    // locale would stop matching them. Matching should not depend on the JVM's
    // default locale.
    @Test
    // Mutates JVM-wide default Locale: needs exclusive access in case tests
    // are ever run in parallel
    @ResourceLock(Resources.LOCALE)
    public void testMatchingIndependentOfDefaultLocale()
    {
        final Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));

            CaseInsensitiveNameSet set = CaseInsensitiveNameSet.construct(
                    Collections.singleton("ITEM"));
            assertTrue(set.contains("item"),
                    "case-insensitive match of 'item' against 'ITEM' should hold under any locale");

            set = CaseInsensitiveNameSet.construct(Collections.singleton("title"));
            assertTrue(set.contains("TITLE"),
                    "case-insensitive match of 'TITLE' against 'title' should hold under any locale");
        } finally {
            Locale.setDefault(orig);
        }
    }
}
