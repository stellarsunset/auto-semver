package com.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Map;

public class ReleaseTask extends DefaultTask {

    private static final Version.Serde SERDE = Version.Serde.java();

    private final GitW git;

    @Inject
    public ReleaseTask(Git git) {
        this.git = new GitW(git);
    }

    @Override
    public String getDescription() {
        return "Tag the current commit as a release commit with the provided increment (default: Patch)";
    }

    // https://docs.gradle.org/current/userguide/custom_tasks.html
    @TaskAction
    public void release() {
        Logger logger = getLogger();

        Project project = getProject();
        CliOptions options = new CliOptions(project.getProperties());

        Version version = SERDE.parse((String) project.getVersion());
        Version.Release previous = Version.releasePart(version);

        Version.Release next = options.nextVersion(previous);
        Ref ref = git.version(next);

        logger.info("Tagged commit {} as release {}", ref.getName(), SERDE.serialize(next));
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
    }
}