package io.github.stellarsunset.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

class GitHelpers {

    static void initializeProjectSafely(File projectDir) {
        try (Git git = initializeProject(projectDir)) {
            // Do nothing
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    /**
     * Initialize a new gradle project + git repo in the provided directory.
     */
    static Git initializeProject(File projectDir) throws Exception {

        Git main = Git.init()
                .setGitDir(projectDir)
                .setInitialBranch("main")
                .call();

        File testFile = new File(projectDir, "test.txt");
        writeString(testFile, "");

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
