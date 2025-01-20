package io.github.stellarsunset.semver;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ReleaseTask extends DefaultTask {

    private static final Version.Serde SERDE = Version.Serde.java();

    private final Git git;
    private Version version;

    @Inject
    public ReleaseTask(Git git) {
        this.git = requireNonNull(git);
    }

    @Override
    public String getDescription() {
        return "Tag the current commit as a release commit with the provided increment (default: Patch)";
    }

    @Input
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    // https://docs.gradle.org/current/userguide/custom_tasks.html
    @TaskAction
    public void release() {
        Logger logger = getLogger();

        Project project = getProject();
        CliOptions options = new CliOptions(project.getProperties());

        Version.Release previous = Version.releasePart(version);
        Version.Release next = options.nextVersion(previous);

        options.message().map(m -> git.tagVersion(next, m))
                .orElseGet(() -> git.tagVersion(next));

        logger.lifecycle("Tagged new release {}", SERDE.serialize(next));
    }

    private record CliOptions(Map<String, ?> properties) {

        boolean incrementPatch() {
            return properties.containsKey("patch");
        }

        boolean incrementMinor() {
            return properties.containsKey("minor");
        }

        boolean incrementMajor() {
            return properties.containsKey("major");
        }

        private Version.Release nextVersion(Version.Release previous) {
            if (incrementMajor()) {
                return previous.nextMajor();
            } else if (incrementMinor()) {
                return previous.nextMinor();
            } else {
                return previous.nextPatch();
            }
        }

        Optional<String> message() {
            return Optional.ofNullable(properties.get("message")).map(o -> o instanceof String s ? s : null);
        }
    }
}