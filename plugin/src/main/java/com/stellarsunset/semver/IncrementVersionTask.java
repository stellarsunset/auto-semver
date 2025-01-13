package com.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class IncrementVersionTask extends DefaultTask {

    private final Git git;

    @Inject
    public IncrementVersionTask(Git git) {
        this.git = requireNonNull(git);
    }

    // https://docs.gradle.org/current/userguide/custom_tasks.html
    @TaskAction
    public void incrementVersion() {
        System.out.println("Hello from plugin 'com.stellarsunset.semver.autosemver'");
    }
}