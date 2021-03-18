package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;

import java.util.List;

public interface PartGrader {

    String name();

    double maxGrade();

    GradePart grade(GitContext c, Exercise exercise);

    default GradePart result(List<String> explanations, double grade) {
        return new GradePart(name(), Math.min(Math.max(0, grade), maxGrade()), Double.valueOf(maxGrade()), explanations);
    }
}
