package com.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import static java.util.Objects.requireNonNull;

public final class IncrementVersionTask extends DefaultTask {

    private final Git git;

    public IncrementVersionTask(Git git) {
        this.git = requireNonNull(git);
    }

    // https://docs.gradle.org/current/userguide/custom_tasks.html
    @TaskAction
    public void incrementVersion() {

    }
}
