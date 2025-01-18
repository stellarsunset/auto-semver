package com.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;


record GitW(Git git) {

    private static final Version.Serde SERDE = Version.Serde.gitPorcelain();

    public Version version() {
        String tag = call(g -> g.describe()
                .setTags(true)
                .setAbbrev(0)
                .call()
        );
        return SERDE.parse(tag);
    }

    public Ref version(Version version) {
        String versionString = SERDE.serialize(version);
        return call(git -> git.tag()
                .setName(versionString)
                .setAnnotated(true)
                .setMessage(String.format("Release version: %s", versionString))
                .call()
        );
    }

    <T> T call(GitFn<Git, T> call) {
        try {
            return call.apply(git);
        } catch (GitAPIException e) {
            IllegalStateException exception = new IllegalStateException("Error performing Git API operation.");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @FunctionalInterface
    interface GitFn<I, O> {
        O apply(I i) throws GitAPIException;
    }
}
