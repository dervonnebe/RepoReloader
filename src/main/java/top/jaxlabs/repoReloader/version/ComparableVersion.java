package top.jaxlabs.repoReloader.version;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic-version aware comparator. Strips leading 'v'/'V', parses numeric core
 * parts and an optional pre-release identifier (SemVer-style).
 * Pre-release versions sort lower than their release counterpart (e.g. 1.0.0-rc1 < 1.0.0).
 */
public final class ComparableVersion implements Comparable<ComparableVersion> {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)*)(?:-([0-9A-Za-z.-]+))?.*");

    private final List<Integer> coreParts;
    private final String preRelease;
    private final boolean valid;

    private ComparableVersion(List<Integer> coreParts, String preRelease, boolean valid) {
        this.coreParts = coreParts;
        this.preRelease = preRelease;
        this.valid = valid;
    }

    /**
     * Returns {@code true} when {@code remoteTag} represents a newer version than {@code localVersion}.
     */
    public static boolean isNewer(String remoteTag, String localVersion) {
        ComparableVersion remote = parse(remoteTag);
        ComparableVersion local = parse(localVersion);

        if (!remote.valid || !local.valid) {
            return !stripLeadingV(remoteTag).equalsIgnoreCase(stripLeadingV(localVersion));
        }
        return remote.compareTo(local) > 0;
    }

    public static ComparableVersion parse(String value) {
        String normalized = stripLeadingV(value);
        Matcher matcher = VERSION_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return new ComparableVersion(List.of(), "", false);
        }

        String numberPart = matcher.group(1);
        String preReleasePart = matcher.group(2) == null ? "" : matcher.group(2);
        String[] split = numberPart.split("\\.");
        List<Integer> numbers = new ArrayList<>(split.length);
        for (String item : split) {
            try {
                numbers.add(Integer.parseInt(item));
            } catch (NumberFormatException ignored) {
                return new ComparableVersion(List.of(), "", false);
            }
        }

        return new ComparableVersion(numbers, preReleasePart, true);
    }

    @Override
    public int compareTo(ComparableVersion other) {
        int maxLength = Math.max(coreParts.size(), other.coreParts.size());
        for (int i = 0; i < maxLength; i++) {
            int a = i < coreParts.size() ? coreParts.get(i) : 0;
            int b = i < other.coreParts.size() ? other.coreParts.get(i) : 0;
            if (a != b) return Integer.compare(a, b);
        }

        boolean thisPre = !preRelease.isBlank();
        boolean otherPre = !other.preRelease.isBlank();
        if (thisPre && !otherPre) return -1;
        if (!thisPre && otherPre) return 1;
        if (!thisPre) return 0;

        List<String> left = splitPreRelease(preRelease);
        List<String> right = splitPreRelease(other.preRelease);
        int parts = Math.max(left.size(), right.size());
        for (int i = 0; i < parts; i++) {
            if (i >= left.size()) return -1;
            if (i >= right.size()) return 1;

            String l = left.get(i);
            String r = right.get(i);
            boolean lNum = isNumeric(l);
            boolean rNum = isNumeric(r);

            if (lNum && rNum) {
                int cmp = Integer.compare(Integer.parseInt(l), Integer.parseInt(r));
                if (cmp != 0) return cmp;
                continue;
            }
            if (lNum != rNum) return lNum ? -1 : 1;

            int cmp = l.compareToIgnoreCase(r);
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    private static String stripLeadingV(String value) {
        if (value == null) return "";
        String v = value.trim();
        return (v.startsWith("v") || v.startsWith("V")) ? v.substring(1) : v;
    }

    private static List<String> splitPreRelease(String value) {
        if (value == null || value.isBlank()) return List.of();
        String[] split = value.split("\\.");
        List<String> result = new ArrayList<>(split.length);
        for (String part : split) {
            if (!part.isBlank()) result.add(part);
        }
        return result;
    }

    private static boolean isNumeric(String value) {
        if (value.isEmpty()) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }
}
