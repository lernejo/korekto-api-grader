package com.github.lernejo.korekto.grader.api.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.misc.Ports;
import com.github.lernejo.korekto.toolkit.misc.ThrowingFunction;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutionHandle;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Part4Grader implements PartGrader {

    private final GradingConfiguration configuration;
    private final MavenContext context;
    private final MeetMockApiClient client;
    private final Random random = new SecureRandom();

    public Part4Grader(GradingConfiguration configuration, MavenContext context, MeetMockApiClient client) {
        this.configuration = configuration;
        this.context = context;
        this.client = client;
    }

    @Override
    public String name() {
        return "Part 4 - API Behavior";
    }

    @Override
    public double maxGrade() {
        return 4.0D;
    }

    @Override
    public GradePart grade(GitContext c, Exercise exercise) {
        if (context.compilationFailed) {
            context.apiStatusFailed = true;
            return result(List.of("Not trying to start server as compilation failed"), 0.0D);
        }
        try
            (MavenExecutionHandle handle = MavenExecutor.executeGoalAsync(exercise, configuration.getWorkspace(),
                "org.springframework.boot:spring-boot-maven-plugin:2.4.4:run -Dspring-boot.run.jvmArguments='-Dserver.port=8085 -DtackEnabled=true'");
             AgifyServer agifyServer = AgifyServer.createStarted()) {

            UserInfo user1 = new UserInfo(agifyServer.addUser(generateName(), 23, "FR"), "M", "F");
            UserInfo user2 = new UserInfo(agifyServer.addUser(generateName(), 22, "FR"), "F", "M");
            UserInfo user3 = new UserInfo(agifyServer.addUser(generateName(), 51, "FR"), "M", "F");
            UserInfo user4 = new UserInfo(agifyServer.addUser(generateName(), 24, "IS"), "M", "M");
            UserInfo user5 = new UserInfo(agifyServer.addUser(generateName(), 22, "IS"), "M", "M");

            Ports.waitForPortToBeListenedTo(8085, TimeUnit.SECONDS, 20L);
            Ports.waitForPortToBeListenedTo(9876, TimeUnit.SECONDS, 5L);

            List<Response<ResponseBody>> collect = Stream.of(
                user1,
                user2,
                user3,
                user4,
                user5
            )
                .map(ThrowingFunction.sneaky(ui -> client.submitInscription(ui).execute()))
                .collect(Collectors.toList());
            Optional<Response<ResponseBody>> unsuccessfulResponse = collect.stream().filter(r -> !r.isSuccessful()).findFirst();
            if (!unsuccessfulResponse.isEmpty()) {
                context.apiStatusFailed = true;
                int code = unsuccessfulResponse.get().code();
                if (code == 404) {
                    return result(List.of("`POST /api/inscription` not implemented (404)"), 0.0D);
                } else {
                    return result(List.of("`POST /api/inscription` not working as expected (expected code 2XX, got: " + code + ")"), maxGrade() / 8);
                }
            }

            Response<List<UserMatch>> user2Matches = client.getMatches(user2.userName, user2.userCountry).execute();
            Response<List<UserMatch>> user3Matches = client.getMatches(user3.userName, user3.userCountry).execute();
            if (!user2Matches.isSuccessful() || !user3Matches.isSuccessful()) {
                context.apiStatusFailed = true;
                if (user2Matches.code() == 404) {
                    return result(List.of("`GET /api/matches` not implemented (404)"), maxGrade() / 4);
                } else {
                    int matchesCode = user2Matches.isSuccessful() ? user3Matches.code() : user2Matches.code();
                    return result(List.of("`GET /api/matches` not working as expected (expected code 2XX, got: " + matchesCode + ")"), 3 * maxGrade() / 8);
                }
            } else {
                if (!user2Matches.body().equals(List.of(user1.asMatch()))) {
                    return result(List.of("Logic not correctly implemented, " + user2 + " should have " + user1 + " as a match"), maxGrade() / 2);
                } else if (!user3Matches.body().isEmpty()) {
                    return result(List.of("Logic not correctly implemented, " + user3 + " should have no match but found: " + user3Matches.body()), maxGrade() / 2);
                } else {
                    return result(List.of(), maxGrade());
                }
            }
        } catch (RuntimeException e) {
            return result(List.of("Server failed to start within 20 sec."), 0.0D);
        } catch (IOException e) {
            return result(List.of("Fail to call server: " + e.getMessage()), 0.0D);
        }
    }

    private final List<String> usedNames = new ArrayList<>();

    private String generateName() {
        String name;
        do {
            name = NAMES.get(random.nextInt(NAMES.size()));
        } while (usedNames.contains(name));
        usedNames.add(name);
        return name;
    }

    private static final List<String> NAMES = List.of(
        "Bao",
        "Carmel",
        "Claudy",
        "Alaa",
        "Alvine",
        "Amal",
        "Angy",
        "Ganael",
        "Hilal",
        "Hong",
        "Iliane",
        "Jael",
        "Devy",
        "Dani",
        "Danny",
        "Ilkay",
        "Ivane",
        "Josua",
        "Kama",
        "Kande",
        "Elvan",
        "Engy",
        "Camerone",
        "Chan",
        "Donat",
        "Eden",
        "Ely",
        "Kamille",
        "Kerry"
    );
}
