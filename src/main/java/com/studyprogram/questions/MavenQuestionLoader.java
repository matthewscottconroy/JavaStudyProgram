package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class MavenQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.MAVEN; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("mvn-mc-01", Topic.MAVEN, 1,
                "What file defines a Maven project's configuration and dependencies?",
                "build.gradle", "pom.xml", "settings.json", "manifest.mf",
                "b",
                "pom.xml (Project Object Model) is Maven's build descriptor. "
                + "It declares the project's groupId, artifactId, version, dependencies, and plugins. "
                + "build.gradle belongs to Gradle."),

            mc("mvn-mc-02", Topic.MAVEN, 1,
                "Which Maven command compiles, tests, and packages the project into a JAR?",
                "mvn build", "mvn compile", "mvn package", "mvn install-jar",
                "c",
                "'mvn package' runs the full lifecycle through the package phase: validate → compile → test → package. "
                + "'mvn compile' only compiles. 'mvn build' does not exist. "
                + "'mvn install' also copies the JAR to the local repository."),

            mc("mvn-mc-03", Topic.MAVEN, 2,
                "What does the 'test' dependency scope mean?",
                "The dependency is only available at compile time",
                "The dependency is available everywhere including production",
                "The dependency is only on the classpath during testing and is not packaged",
                "The dependency is downloaded only when tests fail",
                "c",
                "Scope 'test' restricts the dependency to the test compile and test runtime classpaths. "
                + "JUnit and Mockito are typical test-scoped dependencies — they must not ship in the production JAR."),

            mc("mvn-mc-04", Topic.MAVEN, 2,
                "What is the purpose of 'mvn clean'?",
                "Removes all source files",
                "Deletes the target/ directory (compiled classes, JARs, reports)",
                "Resets the local Maven repository",
                "Removes all test failures from the build log",
                "b",
                "'mvn clean' deletes the target/ directory, ensuring the next build starts fresh. "
                + "It's often combined: 'mvn clean package' to avoid stale compiled files."),

            trace("mvn-tr-01", Topic.MAVEN, 2,
                "What is the standard directory for Java source files in a Maven project?",
                "// This is a conceptual question about Maven project layout.\n"
                + "// Where does Maven expect main Java source files?",
                "src/main/java",
                "Maven's standard directory layout: src/main/java for production code, "
                + "src/test/java for tests, src/main/resources for resource files."),

            debug("mvn-db-01", Topic.MAVEN, 2,
                "The build fails with 'package org.junit.jupiter.api does not exist'. Why?",
                "<dependency>\n"
                + "  <groupId>org.junit.jupiter</groupId>\n"
                + "  <artifactId>junit-jupiter</artifactId>\n"
                + "  <version>5.10.0</version>\n"
                + "</dependency>",
                "JUnit 5 is not compatible with Maven",
                "The dependency is missing a <scope>test</scope> — so Maven excludes it from compilation",
                "The dependency has no scope, defaulting to 'compile', but the class isn't found — likely a network issue or missing version",
                "The version number is invalid",
                "b",
                "Actually the most common cause of this: missing <scope>test</scope> does NOT cause compile failures for the main source. "
                + "More likely: the dependency is absent from the local repo (run 'mvn dependency:resolve') "
                + "or the artifactId/groupId is wrong. In this question, adding <scope>test</scope> is best practice "
                + "even if not the direct cause here."),

            codegen("mvn-cg-01", Topic.MAVEN, 2,
                "Which pom.xml snippet correctly adds JUnit 5 as a test dependency?",
                "<dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.13</version></dependency>",
                "<dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.10.0</version><scope>test</scope></dependency>",
                "<dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.10.0</version></dependency>",
                "<testDependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId></testDependency>",
                "b",
                "Option B uses the correct JUnit 5 groupId/artifactId and the test scope. "
                + "Option A is JUnit 4. "
                + "Option C omits <scope>test</scope> — JUnit would ship in the production JAR. "
                + "Option D uses a non-existent XML element."),

            mc("mvn-mc-05", Topic.MAVEN, 2,
                "What does 'mvn clean' do?",
                "Deletes all .java source files",
                "Removes the 'target' directory containing compiled classes and JARs",
                "Uninstalls all dependencies from the local repository",
                "Runs all unit tests and generates a report",
                "b",
                "mvn clean deletes the target/ directory, removing compiled .class files and built JARs. "
                + "Use it before a fresh build to avoid stale artifacts. "
                + "mvn clean package is a common combined command: clean, then compile and package."),

            trace("mvn-tr-02", Topic.MAVEN, 2,
                "What directory does Maven place compiled .class files in?",
                "// pom.xml with default settings, sources in src/main/java\n"
                + "// After running: mvn compile",
                "target/classes",
                "Maven's default output directory for compiled main sources is target/classes. "
                + "Test class files go to target/test-classes."),

            mc("mvn-mc-06", Topic.MAVEN, 3,
                "What is the Maven local repository?",
                "The src/main directory of your project",
                "A local cache (~/.m2/repository) where downloaded dependencies are stored",
                "The GitHub repository where Maven plugins are hosted",
                "A directory inside target/ where JARs are staged",
                "b",
                "When Maven downloads a dependency, it caches it in ~/.m2/repository. "
                + "Subsequent builds use the cached version without re-downloading. "
                + "You can force re-download with: mvn dependency:resolve -U"),

            codegen("mvn-cg-02", Topic.MAVEN, 2,
                "Which Maven command compiles, runs all tests, and creates the JAR?",
                "mvn compile",
                "mvn test",
                "mvn package",
                "mvn install",
                "c",
                "mvn package runs the full lifecycle through package: compile → test → package (creates the JAR). "
                + "mvn compile stops at compile. mvn test stops at test. "
                + "mvn install goes further: also installs the JAR to the local repository."),

            mc("mvn-mc-07", Topic.MAVEN, 2,
                "What are the three required coordinates to identify a Maven artifact?",
                "name, version, classifier",
                "groupId, artifactId, version",
                "namespace, module, build",
                "project, component, edition",
                "b",
                "Maven coordinates: groupId (org/company), artifactId (project name), version (1.0.0). "
                + "Together they uniquely identify a dependency in any repository. "
                + "Example: groupId=com.google.guava, artifactId=guava, version=32.0.0-jre."),

            mc("mvn-mc-08", Topic.MAVEN, 2,
                "Which Maven lifecycle phase runs unit tests?",
                "compile", "validate", "test", "verify",
                "c",
                "Maven's default lifecycle: validate → compile → test → package → verify → install → deploy. "
                + "Running 'mvn test' executes all phases up to and including test. "
                + "To skip tests: mvn package -DskipTests (not recommended for production builds)."),

            mc("mvn-mc-09", Topic.MAVEN, 3,
                "What is the purpose of the <parent> element in a Maven pom.xml?",
                "It specifies the Java version to use",
                "It inherits configuration (version management, plugin defaults) from a parent POM",
                "It declares the project's main class",
                "It points to the remote Maven repository",
                "b",
                "<parent> makes this project inherit from a parent POM. "
                + "Spring Boot starter parent is the most common example: it manages all dependency versions. "
                + "The Bill of Materials (BOM) pattern uses parent POMs for consistent version management."),

            trace("mvn-tr-03", Topic.MAVEN, 2,
                "What directory does 'mvn test' create for test reports?",
                "// After: mvn test\n"
                + "// Project has passing and failing JUnit tests.",
                "target/surefire-reports",
                "The Maven Surefire plugin runs tests and writes XML and TXT reports to target/surefire-reports. "
                + "Check this directory for test failure details when running in CI."),

            trace("mvn-tr-04", Topic.MAVEN, 3,
                "What does 'mvn dependency:tree' show?",
                "// Run: mvn dependency:tree",
                "The full transitive dependency tree of the project",
                "dependency:tree displays direct and transitive dependencies in tree format. "
                + "Useful for diagnosing version conflicts: if two dependencies require different versions of the same library, Maven uses 'nearest wins' resolution."),

            debug("mvn-db-02", Topic.MAVEN, 3,
                "The build succeeds but tests are not run. Why?",
                "// pom.xml running 'mvn package -DskipTests'",
                "The Surefire plugin is not installed",
                "The '-DskipTests' flag explicitly skips test execution",
                "Tests run only with 'mvn test', not 'mvn package'",
                "No test source directory is configured",
                "b",
                "-DskipTests skips both compiling and running tests. "
                + "-Dmaven.test.skip=true also skips compilation. "
                + "Remove the flag to re-enable tests. "
                + "Never ship to production with skipped tests."),

            debug("mvn-db-03", Topic.MAVEN, 2,
                "The project fails with 'Plugin org.apache.maven.plugins:maven-compiler-plugin not found'. Why?",
                "// pom.xml with no network connection to Maven Central",
                "The compiler plugin must be declared in <dependencies>",
                "Maven cannot download the compiler plugin — check the repository URL and network connection",
                "The plugin is not available for this Java version",
                "The plugin should be in <build> not <plugins>",
                "b",
                "Maven plugins are downloaded from Maven Central by default. "
                + "A network issue or incorrect repository configuration prevents the download. "
                + "Fix: check network, check <repositories> and <pluginRepositories> in pom.xml, "
                + "or run offline with a pre-populated local repo."),

            codegen("mvn-cg-03", Topic.MAVEN, 2,
                "Which pom.xml snippet configures the Java compiler to use Java 17?",
                "<java.version>17</java.version>",
                "<properties><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>",
                "<build><compiler>17</compiler></build>",
                "<dependency><groupId>java</groupId><artifactId>compiler</artifactId><version>17</version></dependency>",
                "b",
                "maven.compiler.source and target control the Java version for compilation. "
                + "Both must be set. For Java 9+, you can also use maven.compiler.release=17. "
                + "Option A: spring-boot-starter-parent reads java.version but standalone Maven projects do not. "
                + "Options C and D are invalid.")
        );
    }
}
