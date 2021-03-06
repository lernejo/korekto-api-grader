package com.github.lernejo.korekto.grader.api;

import com.github.lernejo.korekto.grader.api.parts.*;
import com.github.lernejo.korekto.toolkit.*;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitNature;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ApiGrader implements Grader {

    private final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://localhost:8085/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    public final MeetMockApiClient client = retrofit.create(MeetMockApiClient.class);

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
        MavenContext mavenContext = new MavenContext();

        return List.of(
            new Part1Grader(configuration, mavenContext).grade(git, exercise),
            new Part2Grader().grade(git, exercise),
            new Part3Grader(configuration, mavenContext).grade(git, exercise),
            new Part4Grader(configuration, mavenContext, client).grade(git, exercise),
            new Part5Grader(configuration, mavenContext, client).grade(git, exercise),
            new Part6Grader().grade(git, exercise),
            new Part7Grader().grade(git, exercise)
        );
    }

    @Override
    public Instant deadline(GradingContext context) {
        return Instant.parse("2021-04-06T23:59:00.00Z");
    }

    @Override
    public String slugToRepoUrl(String slug) {
        return "https://github.com/" + slug + "/api_training";
    }
}
