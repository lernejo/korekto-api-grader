package com.github.lernejo.korekto.grader.api;

import com.github.lernejo.korekto.toolkit.GradingJob;

public class Launcher {

    public static void main(String[] args) {
        int exitCode = new GradingJob()
            .addCloneStep()
            .addStep("grading", new Grader())
            .addSendStep()
            .run();
        System.exit(exitCode);
    }
}
