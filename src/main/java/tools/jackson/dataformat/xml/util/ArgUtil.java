package tools.jackson.dataformat.xml.util;

public abstract class ArgUtil
{
    public static String emptyToNull(String str) {
        return "".equals(str) ? null : str;
    }

    public static String nullToEmpty(String str) {
        return (str == null) ? "" : str;
    }

    public static String nonEmptyNonNull(String prop, String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Illegal argument for '%s': must be non-empty String"
                    .formatted(prop));
        }
        return str;
    }
}
