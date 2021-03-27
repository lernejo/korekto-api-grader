package com.github.lernejo.korekto.grader.api.parts;

public class UserInfo {

    public final String userEmail;
    public final String userName;
    public final String userTweeter;
    public final String userCountry;
    public final String userSex;
    public final String userSexPref;
    private final int age;

    public UserInfo(AgifyServer.LocalizedAgifyUser user, String userSex, String userSexPref) {
        this(user.name + "@titi.com", user.name.substring(0, 1).toUpperCase() + user.name.substring(1), user.name, user.country_id, userSex, userSexPref, user.age);
    }

    public UserInfo(String userEmail, String userName, String userTweeter, String userCountry, String userSex, String userSexPref, int age) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.userTweeter = userTweeter;
        this.userCountry = userCountry;
        this.userSex = userSex;
        this.userSexPref = userSexPref;
        this.age = age;
    }

    @Override
    public String toString() {
        return userName + "(" + age + ", " + userSex + "->" + userSexPref + ", " + userCountry + ")";
    }

    UserMatch asMatch() {
        return new UserMatch(userName, userTweeter);
    }
}
