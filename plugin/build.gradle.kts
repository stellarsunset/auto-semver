plugins {
    id("com.gradle.plugin-publish") version "1.3.0"
    jacoco
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.eclipse.org/content/groups/releases")
        mavenContent {
            releasesOnly()
        }
    }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.jgit)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = "io.github.stellarsunset"

gradlePlugin {
    website.set("https://github.com/stellarsunset/auto-semver")
    vcsUrl.set("https://github.com/stellarsunset/auto-semver")
    val autoSemver by plugins.creating {
        id = "io.github.stellarsunset.auto-semver"
        implementationClass = "io.github.stellarsunset.semver.AutoSemverPlugin"
        displayName = "Automatic semantic versioning plugin"
        description = "Lightweight auto semantic versioning plugin based on annotated git tags."
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.javadoc {
    options.outputLevel = JavadocOutputLevel.QUIET
}