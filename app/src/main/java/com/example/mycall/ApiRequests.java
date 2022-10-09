package com.example.mycall;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiRequests {

    @GET("generateToken")
    Call<JsonObject> generateCallToken(@Query("channel_name") String channel_name,
                                  @Query("role") int role,
                                  @Query("uid") int uid
                                  );

    @POST("sendCallNotification")
    Call<JsonObject> sendCallNotificationToRemoteUser(
            @Query("channelToken") String channelToken,
            @Query("channel_name") String channel_name,
            @Query("type") String type,
            @Body NotificationToken notificationToken);
}
