package io.github.stellarsunset.semver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the bounded collection of supported version formats.
 *
 * <p>The version interface only guarantees there is a string format for each version through {@link Serde}.
 */
public sealed interface Version {

    /**
     * The initial release version for a project if there are no tags already present.
     */
    static Release initial() {
        return new Release(0, 0, 1);
    }

    /**
     * Handle for a release version of software containing the standard semantic versioning components one would expect.
     */
    static Release release(int major, int minor, int patch) {
        return new Release(major, minor, patch);
    }

    /**
     * Handle for a pre-release version, the release indicates the version it's a preview of, how many commits it's been
     * since the previous release, and the hash of the current commit.
     */
    static PreRelease preRelease(Release release, int distance, String commit) {
        return new PreRelease(release, distance, commit);
    }

    /**
     * Wrap a version object as "dirty" indicating there are currently unstaged changes in the repository at the time of
     * version generation.
     */
    static Dirty dirty(Version version) {
        return new Dirty(version);
    }

    /**
     * Returns only the {@link Release} portion of the underlying version, useful for getting a handle on the previous
     * release given the current version and then incrementing it.
     */
    static Release releasePart(Version version) {
        return switch (version) {
            case Version.Release r -> r;
            case Version.PreRelease p -> p.release();
            case Version.Dirty d -> releasePart(version);
        };
    }

    record Release(int major, int minor, int patch) implements Version {
        public Release {
            checkArgument(major >= 0, "Major version must be non-negative: %s", major);
            checkArgument(minor >= 0, "Minor version must be non-negative: %s", minor);
            checkArgument(patch >= 0, "Patch version must be non-negative: %s", patch);
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

    record PreRelease(Release release, int distance, String commit) implements Version {
        public PreRelease {
            checkArgument(distance > 0, "Distance must be greater than zero for a snapshot: %s", distance);
            checkArgument(commit.length() >= 7, "Git commit SHA should be at least the 7 character recommendation: %s", commit);
        }
    }

    record Dirty(Version version) implements Version {
    }

    /**
     * Serialization and deserialization mechanics for {@link Version}s.
     *
     * <p>This is provided outside the scope of {@link Object#toString()} and in a standalone class to decouple the Java
     * representation of the object as a string from how it's serialized as what people would expect in a normal version
     * string.
     */
    interface Serde {

        /**
         * Returns a new {@link Serde} for versions that is compliant with the SemVer specification, intended for use in
         * Java applications.
         *
         * <p>Use this as your Gradle build version.
         */
        static Serde java() {
            return new Java();
        }

        /**
         * Returns a new {@link Serde} for versions suitable for reading and writing them in the format the git-describe
         * command in the porcelain API provides.
         *
         * <p>Use this for your git tags.
         */
        static Serde gitPorcelain() {
            return new GitPorcelain();
        }

        String serialize(Version version);

        Version parse(String versionString);

        final class IllegalVersionException extends RuntimeException {
            public IllegalVersionException(String versionString) {
                super(String.format("Unable to parse version string %s into one of the supported version formats.", versionString));
            }
        }

        /**
         * Version string parser supporting custom release and pre-release regexes assuming they have the required named
         * capture groups.
         *
         * <p>This code is re-usable enough to carve out into something standalone.
         */
        record RegexParser(Pattern releasePattern, Pattern preReleasePattern) {

            public Version parse(String versionString) {

                Matcher releaseMatcher = releasePattern.matcher(versionString);

                if (releaseMatcher.find()) {
                    return parseRelease(releaseMatcher);
                }

                Matcher preReleaseMatcher = preReleasePattern.matcher(versionString);

                if (preReleaseMatcher.find()) {
                    Release release = parseRelease(preReleaseMatcher);

                    PreRelease preRelease = preRelease(
                            release,
                            Integer.parseInt(preReleaseMatcher.group("distance")),
                            preReleaseMatcher.group("commit")
                    );

                    return versionString.endsWith(".dirty") ? dirty(preRelease) : preRelease;
                }

                throw new IllegalVersionException(versionString);
            }

            private Release parseRelease(Matcher matcher) {
                return release(
                        Integer.parseInt(matcher.group("major")),
                        Integer.parseInt(matcher.group("minor")),
                        Integer.parseInt(matcher.group("patch"))
                );
            }
        }

        record Java() implements Serde {

            private static final RegexParser PARSER = new RegexParser(
                    Pattern.compile("^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)$"),
                    Pattern.compile("^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)-alpha(?<distance>0|[1-9]\\d*)\\+(?<commit>[a-z]{7})(\\.dirty)?$")
            );

            @Override
            public String serialize(Version version) {
                return switch (version) {
                    case Dirty d -> String.format("%s.dirty", serialize(d.version));
                    case Release r -> String.format("%s.%s.%s", r.major, r.minor, r.patch);
                    case PreRelease s -> String.format("%s-alpha%s+%s", serialize(s.release), s.distance, s.commit);
                };
            }

            @Override
            public Version parse(String versionString) {
                return PARSER.parse(versionString);
            }
        }

        record GitPorcelain() implements Serde {

            private static final RegexParser PARSER = new RegexParser(
                    Pattern.compile("^v(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)$"),
                    Pattern.compile("^v(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)-(?<distance>0|[1-9]\\d*)-(?<commit>[a-z]{7})(\\.dirty)?$")
            );

            @Override
            public String serialize(Version version) {
                return switch (version) {
                    case Dirty d -> String.format("%s.dirty", serialize(d.version));
                    case Release r -> String.format("v%s.%s.%s", r.major, r.minor, r.patch);
                    case PreRelease s -> String.format("%s-%s-%s", serialize(s.release), s.distance, s.commit);
                };
            }

            @Override
            public Version parse(String versionString) {
                return PARSER.parse(versionString);
            }
        }
    }
}
