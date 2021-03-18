package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenJacocoReport;

import java.util.List;
import java.util.Optional;

import static com.github.lernejo.korekto.toolkit.misc.Maths.round;

public class Part3Grader implements PartGrader {

    private final GradingConfiguration configuration;
    private final MavenContext context;

    public Part3Grader(GradingConfiguration configuration, MavenContext context) {
        this.configuration = configuration;
        this.context = context;
    }

    @Override
    public String name() {
        return "Part 3 - Test Coverage";
    }

    @Override
    public double maxGrade() {
        return 3.0D;
    }

    @Override
    public GradePart grade(GitContext c, Exercise exercise) {
        if (context.testFailed) {
            return result(List.of("Coverage not available when there is test failures"), 0.0D);
        }
        Optional<MavenJacocoReport> jacocoReport = MavenJacocoReport.from(exercise);

        if (jacocoReport.isEmpty()) {
            return result(List.of("No JaCoCo report produced after `mvn verify`, check tests and plugins"), 0D);
        } else {
            double ratio = jacocoReport.get().getRatio();
            if (ratio < 0.8D) {
                double grade = round((ratio * maxGrade()) / 0.8D, 2);
                return result(List.of("Code coverage: " + round(jacocoReport.get().getRatio() * 100, 2) + "%, expected: > 90% with `mvn verify`"), grade);
            } else {
                return result(List.of(), maxGrade());
            }
        }
    }
}
