package com.github.lernejo.korekto.grader.api;

import com.github.lernejo.korekto.toolkit.*;
import com.github.lernejo.korekto.toolkit.misc.Equalator;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitNature;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Grader implements GradingStep {
    private final Equalator equalator = new Equalator(1);

    @Override
    public void run(GradingConfiguration gradingConfiguration, GradingContext context) {
        Optional<GitNature> optionalGitNature = context.getExercise().lookupNature(GitNature.class);
        if (optionalGitNature.isEmpty()) {
            context.getGradeDetails().getParts().add(new GradePart("exercise", 0D, 12D, List.of("Not a Git project")));
        } else {
            GitNature gitNature = optionalGitNature.get();
            context.getGradeDetails().getParts().addAll(gitNature.withContext(c -> grade(gradingConfiguration, c, context.getExercise())));
        }
    }

    private Collection<? extends GradePart> grade(GradingConfiguration configuration, GitContext git, Exercise exercise) {
        return List.of(
        );
    }
}
