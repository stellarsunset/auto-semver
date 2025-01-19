package io.github.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class SetVersionTask extends DefaultTask {

    private static final Version.Serde SERDE = Version.Serde.java();

    private final GitW git;

    @Inject
    public SetVersionTask(Git git) {
        this.git = new GitW(git);
    }

    @Override
    public String getDescription() {
        return "Set the project version based on the distance from the last annotated Git tag in the repository.";
    }

    @TaskAction
    public void setVersion() {
        Project project = getProject();

        Version version = git.version();
        String versionString = SERDE.serialize(version);

        project.setVersion(versionString);
        getLogger().lifecycle("Set Project Version: {}", versionString);
    }
}
