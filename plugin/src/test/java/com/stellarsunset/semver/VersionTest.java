package com.stellarsunset.semver;

import org.junit.jupiter.api.Test;

import static com.stellarsunset.semver.Version.*;
import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    private static final Version.Serde SERDE = Version.serde();

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
    void testSerde_Release() {
        assertAll(
                () -> assertEquals("1.0.0",
                        SERDE.serialize(release(1, 0, 0)), "Serialize"),
                () -> assertEquals(release(1, 0, 0),
                        SERDE.parse("1.0.0"), "Deserialize 1.0.0"),
                () -> assertEquals(release(0, 0, 1),
                        SERDE.parse("0.0.1"), "Deserialize 0.0.1"),
                () -> assertEquals(release(0, 101, 9561),
                        SERDE.parse("0.101.9561"), "Deserialize 0.101.9561")
        );
    }

    @Test
    void testSerde_Snapshot() {
        assertAll(
                () -> assertEquals("1.0.0-alpha1+aaaaaaa",
                        SERDE.serialize(preRelease(release(1, 0, 0), 1, "aaaaaaa")), "Serialize 1.0.0-alpha1+aaaaaaa"),
                () -> assertEquals("1.0.0-alpha105+aabbccz",
                        SERDE.serialize(preRelease(release(1, 0, 0), 105, "aabbccz")), "Serialize 1.0.0-alpha105+aabbccz"),
                () -> assertEquals(preRelease(release(1, 0, 0), 105, "aabbccz"),
                        SERDE.parse("1.0.0-alpha105+aabbccz"), "Deserialize 1.0.0-alpha105+aabbccz"),
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> SERDE.parse("1.0.0-alpha105"), "Deserialize PreRelease without commit"),
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> SERDE.parse("1.0.0+aabbccz"), "Deserialize PreRelease without distance")
        );
    }

    @Test
    void testSerde_Dirty() {
        assertAll(
                () -> assertThrows(Version.Serde.IllegalVersionException.class,
                        () -> SERDE.parse("1.0.0.dirty"), "Deserialize 1.0.0.dirty"),
                () -> assertEquals(dirty(preRelease(release(1, 0, 0), 105, "aabbccz")),
                        SERDE.parse("1.0.0-alpha105+aabbccz.dirty"), "Deserialize 1.0.0-alpha105+aabbccz.dirty")
        );
    }
}
