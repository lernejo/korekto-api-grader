package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.github.GitHubNature;
import com.github.lernejo.korekto.toolkit.thirdparty.github.WorkflowRun;
import com.github.lernejo.korekto.toolkit.thirdparty.github.WorkflowRunConclusion;
import com.github.lernejo.korekto.toolkit.thirdparty.github.WorkflowRunStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Part2Grader implements PartGrader {
    private static final Set<String> mainBranchNames = Set.of("main", "master");

    @Override
    public String name() {
        return "Part 2 - CI";
    }

    @Override
    public double maxGrade() {
        return 1.0D;
    }

    @Override
    public GradePart grade(GitContext c, Exercise exercise) {
        Optional<GitHubNature> gitHubNature = exercise.lookupNature(GitHubNature.class);
        if (gitHubNature.isEmpty()) {
            return result(List.of("Not a GitHub project"), 0D);
        }
        List<WorkflowRun> actionRuns = gitHubNature.get().listActionRuns();

        List<WorkflowRun> mainRuns = actionRuns.stream()
            .filter(wr -> wr.getStatus() == WorkflowRunStatus.completed)
            .filter(wr -> mainBranchNames.contains(wr.getHead_branch()))
            .collect(Collectors.toList());
        if (mainRuns.isEmpty()) {
            return result(List.of("No CI runs for main branch, check https://github.com/" + exercise.getName() + "/actions"), 0D);
        } else {
            WorkflowRun latestRun = mainRuns.get(0);
            if (latestRun.getConclusion() != WorkflowRunConclusion.success) {
                return result(List.of("Latest CI run is not in success state"), maxGrade() / 2);
            } else {
                return result(List.of(), maxGrade());
            }
        }
    }
}
