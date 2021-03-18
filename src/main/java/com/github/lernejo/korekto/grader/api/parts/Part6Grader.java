package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.git.MeaninglessCommit;

import java.util.List;
import java.util.stream.Collectors;

public class Part6Grader {

    public String name() {
        return "Part 6 - Git (proper descriptive messages)";
    }

    public double minGrade() {
        return -4.0D;
    }

    public GradePart grade(GitContext c, Exercise exercise) {
        List<MeaninglessCommit> meaninglessCommits = c.meaninglessCommits();
        List<String> messages = meaninglessCommits.stream()
            .map(mc -> '`' + mc.getShortId() + "` " + mc.getMessage() + " --> " + mc.getReason())
            .collect(Collectors.toList());
        if(messages.isEmpty()) {
            messages.add("OK");
        }
        return new GradePart(name(), Math.max(meaninglessCommits.size() * minGrade() / 8, minGrade()), null, messages);
    }
}
