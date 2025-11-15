package io.github.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

class GitHelpers {

    static Git initializeRepositorySafely(File projectDir) {
        try {
            return initializeRepository(projectDir);
        } catch (Exception e) {
            return Assertions.fail(e);
        }
    }

    /**
     * Initialize a new gradle project w/ plugin configured.
     */
    static void initializeProject(File projectDir) throws IOException {
        File buildFile = new File(projectDir, "build.gradle");

        String content = """
                plugins {
                  id('io.github.stellarsunset.auto-semver')
                }
                
                tasks.register("showVersion") {
                    inputs.property("version", project.version)
                    doLast {
                        println("Project Version: ${inputs.properties["version"]}")
                    }
                }
                """;

        writeString(buildFile, content);

        File settingsFile = new File(projectDir, "settings.gradle");
        writeString(settingsFile, "");
    }

    /**
     * Initialize and commit the initial configuration for our project as a git repository in the directory.
     */
    static Git initializeRepository(File projectDir) throws Exception {

        Git main = Git.init()
                .setGitDir(projectDir)
                .setInitialBranch("main")
                .call();

        initializeProject(projectDir);

        StoredConfig config = main.getRepository().getConfig();
        config.setBoolean("commit", null, "gpgsign", false);
        config.setBoolean("tag", null, "gpgsign", false);
        config.unset("gpg", null, "format");
        config.save();

        RevCommit commit = main.commit()
                .setAuthor("junit", "junit@autosemver.github.com")
                .setMessage("Initial Commit")
                .setAll(true)
                .call();

        return main;
    }

    static void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
