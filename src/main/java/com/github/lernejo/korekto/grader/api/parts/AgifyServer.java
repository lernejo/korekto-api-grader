package com.github.lernejo.korekto.grader.api.parts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class AgifyServer implements AutoCloseable {

    private final HttpServer server;

    private final Map<Key, LocalizedAgifyUser> usersMap = new LinkedHashMap<>();

    private final AtomicInteger callCounter = new AtomicInteger();

    private AgifyServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(9876), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.setExecutor(null);
        server.createContext("/", new CallHandler(usersMap, callCounter));
        server.start();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    static AgifyServer createStarted() {
        return new AgifyServer();
    }

    LocalizedAgifyUser addUser(String name, int age, String country) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        LocalizedAgifyUser user = new LocalizedAgifyUser(lowerName, age, 47, country);
        usersMap.put(new Key(lowerName, country), user);
        return user;
    }

    void resetCallCounter() {
        callCounter.set(0);
    }

    int getCallsNumber() {
        return callCounter.get();
    }

    private static class CallHandler implements HttpHandler {

        private final Gson gson = new GsonBuilder().serializeNulls().create();

        private final Map<Key, LocalizedAgifyUser> usersMap;

        private final AtomicInteger callCounter;

        private CallHandler(Map<Key, LocalizedAgifyUser> usersMap, AtomicInteger callCounter) {
            this.usersMap = usersMap;
            this.callCounter = callCounter;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String rawQuery = exchange.getRequestURI().getQuery();
            Map<String, String> query = Arrays.stream(rawQuery.split("&")).map(q -> q.split("=")).collect(Collectors.toMap(e -> e[0].toLowerCase(), e -> e[1]));

            String name = query.get("name");
            if (name == null) {
                exchange.sendResponseHeaders(422, 0);
                exchange.getResponseBody().close();
            } else {
                callCounter.incrementAndGet();
                String country_id = query.get("country_id");
                AgifyUser agifyUser;
                if (country_id != null) {
                    agifyUser = usersMap.get(new Key(name.toLowerCase(), country_id.toUpperCase()));
                    if (agifyUser == null) {
                        agifyUser = LocalizedAgifyUser.empty(name, country_id);
                    } else {
                        agifyUser = agifyUser.withName(name);
                    }
                } else {
                    agifyUser = usersMap.values().stream()
                        .filter(v -> v.name.equals(name.toLowerCase()))
                        .findFirst()
                        .map(lu -> lu.unLocalize().withName(name))
                        .orElse(AgifyUser.empty(name));
                }
                String body = gson.toJson(agifyUser);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length());
                OutputStream os = exchange.getResponseBody();
                os.write(body.getBytes());
                os.close();
            }
        }
    }

    public static class LocalizedAgifyUser extends AgifyUser {
        public final String country_id;

        LocalizedAgifyUser(String name, Integer age, int count, String country_id) {
            super(name,
                age,
                count);
            this.country_id = country_id;
        }

        @Override
        public String toString() {
            return "AgifyUser{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", country_id=" + country_id +
                '}';
        }

        @Override
        AgifyUser withName(String name) {
            return new LocalizedAgifyUser(name, age, count, country_id);
        }

        AgifyUser unLocalize() {
            return new AgifyUser(name, age, count);
        }

        static AgifyUser empty(String name, String country_id) {
            return new LocalizedAgifyUser(name, null, 0, country_id);
        }
    }

    public static class AgifyUser {
        public final String name;
        public final Integer age;
        public final int count;

        AgifyUser(String name, Integer age, int count) {
            this.name = name;
            this.age = age;
            this.count = count;
        }

        @Override
        public String toString() {
            return "AgifyUser{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
        }

        AgifyUser withName(String name) {
            return new AgifyUser(name, age, count);
        }

        public static AgifyUser empty(String name) {
            return new AgifyUser(name, null, 0);
        }
    }

    private static class Key {
        private final String name;
        private final String country;

        private Key(String name, String country) {
            this.name = name;
            this.country = country;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return name.equals(key.name) && country.equals(key.country);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, country);
        }
    }
}
