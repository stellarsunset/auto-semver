package io.github.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;


record GitW(Git git) {

    private static final Version.Serde SERDE = Version.Serde.gitPorcelain();

    /**
     * Read the version from the latest git tag in the repo.
     */
    public Version version() {
        String tag = call(g -> g.describe()
                .setTags(true)
                .setAbbrev(0)
                .call());
        return tag == null ? Version.initial() : SERDE.parse(tag);
    }

    public Ref version(Version version) {
        return version(version, String.format("Release version: %s", SERDE.serialize(version)));
    }

    /**
     * Set the provided version as an annotated tag in the repo.
     */
    public Ref version(Version version, String message) {
        String versionString = SERDE.serialize(version);
        return call(git -> git.tag()
                .setName(versionString)
                .setAnnotated(true)
                .setMessage(message)
                .call()
        );
    }

    <T> T call(GitFn<Git, T> call) {
        return this.call(call, ErrorFn.demote());
    }

    <T> T call(GitFn<Git, T> call, ErrorFn<GitAPIException, T> errorFn) {
        try {
            return call.apply(git);
        } catch (GitAPIException e) {
            return errorFn.handle(e);
        }
    }

    @FunctionalInterface
    interface GitFn<I, O> {
        O apply(I i) throws GitAPIException;
    }

    @FunctionalInterface
    interface ErrorFn<E extends Exception, O> {

        static <E extends Exception, O> ErrorFn<E, O> demote() {
            return error -> {
                IllegalStateException exception = new IllegalStateException("Error performing Git API operation.");
                exception.addSuppressed(error);
                throw exception;
            };
        }

        /**
         * Handle the error either by doing something and returning a valid value OR by rethrowing the exception as some
         * subclass of a {@link RuntimeException}.
         */
        O handle(E error) throws RuntimeException;
    }
}
