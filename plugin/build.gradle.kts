import com.vanniktech.maven.publish.GradlePublishPlugin
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.gradle.plugin-publish") version "1.3.0"
    jacoco
    id("io.github.stellarsunset.auto-semver") version "0.0.1"
    id("com.vanniktech.maven.publish") version "0.28.0"
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

gradlePlugin {
    website.set("https://github.com/stellarsunset/auto-semver")
    vcsUrl.set("https://github.com/stellarsunset/auto-semver")
    val autoSemver by plugins.creating {
        id = "io.github.stellarsunset.auto-semver"
        implementationClass = "io.github.stellarsunset.semver.AutoSemverPlugin"
        displayName = "Automatic semantic versioning plugin"
        description = "Lightweight automatic semantic versioning plugin based on annotated git tags."
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

mavenPublishing {
    configure(GradlePublishPlugin())

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    coordinates("io.github.stellarsunset", "auto-semver", project.version.toString())

    pom {
        name = "auto-semver"
        description = "Lightweight automatic semantic versioning plugin based on annotated git tags."
        url = "https://github.com/stellarsunset/auto-semver"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "stellarsunset"
                name = "Alex Cramer"
                email = "stellarsunset@proton.me"
            }
        }
        scm {
            connection = "scm:git:git://github.com/stellarsunset/auto-semver.git"
            developerConnection = "scm:git:ssh://github.com/stellarsunset/auto-semver.git"
            url = "http://github.com/stellarsunset/auto-semver"
        }
    }

    signAllPublications()
}