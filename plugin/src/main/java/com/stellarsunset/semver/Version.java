package com.stellarsunset.semver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the bounded collection of supported version formats.
 *
 * <p>The version interface only guarantees there is a string format for each version (via to
 */
public sealed interface Version {

    /**
     * Serialization and deserialization mechanics for Java versions.
     *
     * <p>This is provided outside the scope of {@link Object#toString()} and in a standalone class to decouple the Java
     * representation of the object as a string from how it's serialized as what people would expect in a normal version
     * string.
     */
    static Serde serde() {
        return new Serde();
    }

    /**
     * Handle for a release version of software containing the standard semantic versioning components one would expect.
     */
    record Release(int major, int minor, int patch) implements Version {
        public Release {
            checkArgument(major >= 0, "Major version must be non-negative: %s", major);
            checkArgument(minor >= 0, "Minor version must be non-negative: %s", minor);
            checkArgument(patch >= 0, "Patch version must be non-negative: %s", patch);
        }

        public Release initial() {
            return new Release(0, 0, 1);
        }

        public Release nextMajor() {
            return new Release(major + 1, 0, 0);
        }

        public Release nextMinor() {
            return new Release(major, minor + 1, 0);
        }

        public Release nextPatch() {
            return new Release(major, minor, patch + 1);
        }
    }

    /**
     * Handle for a pre-release version, the release indicates the version it's a preview of, how many commits it's been
     * since the previous release, and the hash of the current commit.
     */
    record PreRelease(Release release, int distance, String commit) implements Version {
        public PreRelease {
            checkArgument(distance > 0, "Distance must be greater than zero for a snapshot: %s", distance);
            checkArgument(commit.length() >= 7, "Git commit SHA should be at least the 7 character recommendation: %s", commit);
        }
    }

    /**
     * Wrap a version object as "dirty" indicating there are currently unstaged changes in the repository at the time of
     * version generation.
     */
    record Dirty(Version version) implements Version {
    }

    /**
     * This is easy enough to convert to an interface if we need to support different string layouts of the same basic
     * versioning information.
     *
     * <p>For now we only support one layout, my layout, lol.
     */
    record Serde() {

        /**
         * Taken directly from <a href="https://semver.org/">semver.org</a>.
         */
        private static final Pattern REGEX = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

        public String serialize(Version version) {
            return switch (version) {
                case Dirty d -> String.format("%s.dirty", serialize(d.version));
                case Release r -> String.format("%s.%s.%s", r.major, r.minor, r.patch);
                case PreRelease s -> String.format("%s-alpha%s+%s", serialize(s.release), s.distance, s.commit);
            };
        }

        public Version parse(String versionString) {
            Matcher matcher = REGEX.matcher(versionString);
            return null;
        }

        private static final class IllegalVersionException extends RuntimeException {
            public IllegalVersionException(String versionString) {
                super(String.format("Unable to parse version string %s into one of the supported version formats.", versionString));
            }
        }
    }
}
