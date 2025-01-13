plugins {
    `java-gradle-plugin`
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

gradlePlugin {
    val autoSemver by plugins.creating {
        id = "com.stellarsunset.semver.autosemver"
        implementationClass = "com.stellarsunset.semver.AutoSemverPlugin"
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