package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutionHandle;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;

import java.io.IOException;
import java.util.List;

public class Part5Grader implements PartGrader {

    private final GradingConfiguration configuration;
    private final MavenContext context;
    private final MeetMockApiClient client;

    public Part5Grader(GradingConfiguration configuration, MavenContext context, MeetMockApiClient client) {
        this.configuration = configuration;
        this.context = context;
        this.client = client;
    }

    @Override
    public String name() {
        return "Part 5 - cache";
    }

    @Override
    public double maxGrade() {
        return 2.0D;
    }

    @Override
    public GradePart grade(GitContext c, Exercise exercise) {
        if (context.apiStatusFailed) {
            return result(List.of("Not trying to start server as API status check failed"), 0.0D);
        }
        try
            (MavenExecutionHandle handle = MavenExecutor.executeGoalAsync(exercise, configuration.getWorkspace(),
                "org.springframework.boot:spring-boot-maven-plugin:2.4.4:run -Dspring-boot.run.jvmArguments='-Dserver.port=8085 -DtackEnabled=true'");
             AgifyServer agifyServer = AgifyServer.createStarted()) {

            UserInfo user1 = new UserInfo(agifyServer.addUser("albert", 23, "FR"), "M", "F");
            UserInfo user2 = new UserInfo(agifyServer.addUser("ginette", 22, "FR"), "F", "M");

            for (int i = 0; i < 10; i++) {
                client.submitInscription(user1).execute();
                client.submitInscription(user2).execute();
                client.getMatches(user1.userName, user1.userCountry).execute();
                client.getMatches(user2.userName, user2.userCountry).execute();
            }

            int callsNumber = agifyServer.getCallsNumber();
            if (callsNumber != 2) {
                return result(List.of("Cache strategy not implemented, expected **2** calls (for 2 distinct users) to api.agify.io, found: **" + callsNumber + "**"), 0.0D);
            } else {
                return result(List.of(), maxGrade());
            }
        } catch (RuntimeException e) {
            return result(List.of("Server failed to start within 20 sec."), 0.0D);
        } catch (IOException e) {
            return result(List.of("Fail to call server: " + e.getMessage()), 0.0D);
        }
    }
}
