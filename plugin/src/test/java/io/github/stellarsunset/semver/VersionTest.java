package io.github.stellarsunset.semver;

import org.junit.jupiter.api.Test;

import static io.github.stellarsunset.semver.Version.*;
import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    private static final Version.Serde JAVA = Version.Serde.java();

    private static final Version.Serde GIT = Version.Serde.gitPorcelain();

    @Test
    void testRelease() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> release(0, 0, -1), "Patch"),
                () -> assertThrows(IllegalArgumentException.class, () -> release(0, -1, 0), "Minor"),
                () -> assertThrows(IllegalArgumentException.class, () -> release(-1, 0, 0), "Major"),
                () -> assertEquals(release(1, 1, 1),
                        release(1, 1, 0).nextPatch(), "Next Major"),
                () -> assertEquals(release(1, 1, 0),
                        release(1, 0, 1).nextMinor(), "Next Major"),
                () -> assertEquals(release(1, 0, 0),
                        release(0, 1, 1).nextMajor(), "Next Major")
        );
    }

    @Test
    void testPreRelease() {
        Version.Release release = Version.release(1, 0, 0);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> preRelease(release, -1, "a".repeat(7)), "Distance"),
                () -> assertThrows(IllegalArgumentException.class, () -> preRelease(release, 1, "a".repeat(4)), "Commit")
        );
    }

    @Test
    void testJavaSerde_Release() {
        assertAll(
                () -> assertEquals("1.0.0",
                        JAVA.serialize(release(1, 0, 0)), "Serialize"),
                () -> assertEquals(release(1, 0, 0),
                        JAVA.parse("1.0.0"), "Deserialize 1.0.0"),
                () -> assertEquals(release(0, 0, 1),
                        JAVA.parse("0.0.1"), "Deserialize 0.0.1"),
                () -> assertEquals(release(0, 101, 9561),
                        JAVA.parse("0.101.9561"), "Deserialize 0.101.9561")
        );
    }

    @Test
    void testJavaSerde_PreRelease() {
        assertAll(
                () -> assertEquals("1.0.0-alpha1+aaaaaaa",
                        JAVA.serialize(preRelease(release(1, 0, 0), 1, "aaaaaaa")), "Serialize 1.0.0-alpha1+aaaaaaa"),
                () -> assertEquals("1.0.0-alpha105+aabbccz",
                        JAVA.serialize(preRelease(release(1, 0, 0), 105, "aabbccz")), "Serialize 1.0.0-alpha105+aabbccz"),
                () -> assertEquals(preRelease(release(1, 0, 0), 105, "aabbccz"),
                        JAVA.parse("1.0.0-alpha105+aabbccz"), "Deserialize 1.0.0-alpha105+aabbccz"),
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> JAVA.parse("1.0.0-alpha105"), "Deserialize PreRelease without commit"),
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> JAVA.parse("1.0.0+aabbccz"), "Deserialize PreRelease without distance")
        );
    }

    @Test
    void testJavaSerde_Dirty() {
        assertAll(
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> JAVA.parse("1.0.0.dirty"), "Deserialize 1.0.0.dirty"),
                () -> assertEquals(dirty(preRelease(release(1, 0, 0), 105, "aabbccz")),
                        JAVA.parse("1.0.0-alpha105+aabbccz.dirty"), "Deserialize 1.0.0-alpha105+aabbccz.dirty")
        );
    }

    @Test
    void testGitSerde_Release() {
        assertAll(
                () -> assertEquals("v1.0.0",
                        GIT.serialize(release(1, 0, 0)), "Serialize"),
                () -> assertEquals(release(1, 0, 0),
                        GIT.parse("v1.0.0"), "Deserialize 1.0.0"),
                () -> assertEquals(release(0, 0, 1),
                        GIT.parse("v0.0.1"), "Deserialize 0.0.1"),
                () -> assertEquals(release(0, 101, 9561),
                        GIT.parse("v0.101.9561"), "Deserialize 0.101.9561")
        );
    }

    @Test
    void testGitSerde_PreRelease() {
        assertAll(
                () -> assertEquals("v1.0.0-1-gaaaaaa1",
                        GIT.serialize(preRelease(release(1, 0, 0), 1, "aaaaaa1")), "Serialize v1.0.0-1-aaaaaaa"),
                () -> assertEquals("v1.0.0-105-ga1bbccz",
                        GIT.serialize(preRelease(release(1, 0, 0), 105, "a1bbccz")), "Serialize v1.0.0-105-aabbccz"),
                () -> assertEquals(preRelease(release(1, 0, 0), 105, "aabbccz"),
                        GIT.parse("v1.0.0-105-gaabbccz"), "Deserialize v1.0.0-105-gaabbccz"),
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> GIT.parse("v1.0.0-105"), "Deserialize PreRelease without commit"),
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> GIT.parse("v1.0.0-gaabbccz"), "Deserialize PreRelease without distance")
        );
    }

    @Test
    void testGitSerde_Dirty() {
        assertAll(
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> GIT.parse("v1.0.0.dirty"), "Deserialize v1.0.0.dirty"),
                () -> assertEquals(dirty(preRelease(release(1, 0, 0), 105, "aabbccz")),
                        GIT.parse("v1.0.0-105-gaabbccz.dirty"), "Deserialize v1.0.0-105-aabbccz.dirty")
        );
    }
}
