package com.github.lernejo.korekto.grader.api.parts;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface MeetMockApiClient {

    @POST("api/inscription")
    @Headers("Content-Type:application/json")
    Call<ResponseBody> submitInscription(@Body UserInfo user);

    @GET("api/matches")
    @Headers("Accept:application/json")
    Call<List<UserMatch>> getMatches(@Query("userName") String name, @Query("userCountry") String country);
}
