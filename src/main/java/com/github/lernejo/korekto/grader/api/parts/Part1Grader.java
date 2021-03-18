package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenInvocationResult;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.PomModifier;
import com.github.lernejo.tack.http.HttpTack;

import java.nio.file.Files;
import java.util.List;

public class Part1Grader implements PartGrader {

    private final GradingConfiguration configuration;
    private final MavenContext context;

    public Part1Grader(GradingConfiguration configuration, MavenContext context) {
        this.configuration = configuration;
        this.context = context;
    }

    @Override
    public String name() {
        return "Part 1 - Compilation & Tests";
    }

    @Override
    public double maxGrade() {
        return 2.0D;
    }

    @Override
    public GradePart grade(GitContext c, Exercise exercise) {
        if (!Files.exists(exercise.getRoot().resolve("pom.xml"))) {
            context.compilationFailed = true;
            context.testFailed = true;
            return result(List.of("Not a Maven project"), 0.0D);
        }
        PomModifier.addRepository(exercise, "jitpack.io", "https://jitpack.io");
        PomModifier.addDependency(exercise, "com.github.lernejo", "http-tack", HttpTack.getVersion());
        HttpTack.installOnSources(exercise.getRoot().resolve("src/main/java"));

        MavenInvocationResult invocationResult = MavenExecutor.executeGoal(exercise, configuration.getWorkspace(), "clean", "test-compile");
        if (invocationResult.getStatus() != MavenInvocationResult.Status.OK) {
            context.compilationFailed = true;
            context.testFailed = true;
            return result(List.of("Compilation failed, see `mvn test-compile`"), 0.0D);
        } else {
            MavenInvocationResult testRun = MavenExecutor.executeGoal(exercise, configuration.getWorkspace(), "verify");
            if (testRun.getStatus() != MavenInvocationResult.Status.OK) {
                context.testFailed = true;
                return result(List.of("There are test failures, see `mvn verify`"), maxGrade() / 2);
            } else {
                return result(List.of(), maxGrade());
            }
        }
    }
}
