package com.github.lernejo.korekto.grader.api.parts;

import java.util.Objects;

public class UserMatch {

    public final String name;
    public final String twitter;

    public UserMatch(String name, String twitter) {
        this.name = name;
        this.twitter = twitter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMatch userMatch = (UserMatch) o;
        return Objects.equals(name, userMatch.name) && Objects.equals(twitter, userMatch.twitter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, twitter);
    }

    @Override
    public String toString() {
        return "UserMatch{" +
            "name='" + name + '\'' +
            ", twitter='" + twitter + '\'' +
            '}';
    }
}
