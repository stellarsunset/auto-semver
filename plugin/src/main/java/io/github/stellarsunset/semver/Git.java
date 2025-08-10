package io.github.stellarsunset.semver;

import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.ProblemReporter;
import org.gradle.api.problems.Severity;
import org.gradle.process.ExecOperations;
import org.gradle.process.internal.ExecException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <a href="https://git-scm.com/book/en/v2/Appendix-B:-Embedding-Git-in-your-Applications-JGit">JGit</a>-esque wrapper
 * for simple Git operations needed to infer and set the project version based on Git tags.
 *
 * <p>This wraps Gradle's managed process API {@link ExecOperations} which is configuration-cache compatible.
 */
@SuppressWarnings("UnstableApiUsage")
public record Git(File projectDir, ExecOperations exec, ProblemReporter reporter) {

    private static final ProblemGroup GIT = ProblemGroup.create("git", "Issues working with local git.");

    private static final Version.Serde SERDE = Version.Serde.gitPorcelain();

    /**
     * Response on stderr from Git if 'git describe' is run on a repo with no tagged commits, we may need to relax this
     * if Git changes this error message.
     */
    private static final String NO_TAGS = "fatal: No names found, cannot describe anything.";

    /**
     * Returns the current repository version as parsed from the stdout of the 'git describe' command on the command line.
     */
    public Version version() {
        return switch (runCommand("git", "describe")) {
            case Result.Success success -> SERDE.parse(success.stdout);
            case Result.Failure failure -> {
                if (failure.stderr.trim().equals(NO_TAGS)) {
                    yield Version.initial();
                }
                var problemId = ProblemId.create("unable-to-describe-latest-tag", "Unable to read latest version tag.", GIT);
                throw reporter.throwing(failure.e, problemId, spec -> spec
                        .severity(Severity.ERROR)
                        .details(failure.stderr)
                        .solution("Ensure the repository exists and has at least one commit.")
                        .withException(failure.e)
                );
            }
        };
    }

    public Git tagVersion(Version.Release version) {
        return tagVersion(version, String.format("Release version: %s", SERDE.serialize(version)));
    }

    /**
     * Tags the current commit with the provided version, only release version are allowed.
     */
    public Git tagVersion(Version.Release version, String message) {
        return switch (runCommand("git", "tag", "-a", SERDE.serialize(version), "-m", message)) {
            case Result.Success _ -> this;
            case Result.Failure failure -> throw reporter.throwing(
                    failure.e,
                    ProblemId.create("unable-to-tag-release", "Unable to tag release.", GIT),
                    spec -> spec
                            .severity(Severity.ERROR)
                            .details(failure.stderr)
                            .solution("Ensure the current commit is not dirty and is not already tagged.")
                            .withException(failure.e)
            );
        };
    }

    private sealed interface Result {
        record Success(String stdout) implements Result {
        }

        record Failure(String stderr, Exception e) implements Result {
        }
    }

    private Result runCommand(String... commandLine) {
        var out = new ByteArrayOutputStream();
        var err = new ByteArrayOutputStream();
        try {
            exec.exec(spec -> spec
                    .commandLine(List.of(commandLine))
                    .setStandardOutput(out)
                    .setErrorOutput(err)
                    .setWorkingDir(projectDir)
            ).assertNormalExitValue();
            return new Result.Success(out.toString(StandardCharsets.UTF_8));
        } catch (ExecException e) {
            return new Result.Failure(err.toString(StandardCharsets.UTF_8), e);
        }
    }
}
