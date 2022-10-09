package com.example.mycall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationToken {
    String phoneNumber;
    String token;

    public NotificationToken() {
    }

    public NotificationToken(String phoneNumber, String token) {
        this.phoneNumber = phoneNumber;
        this.token = token;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    // Only for Sending Notification to remote user through Node js Server...
    @SerializedName("fcm_tokens")
    @Expose
    String[] fcmTokens;

    public NotificationToken(String[] fcmTokens) {
        this.fcmTokens = fcmTokens;
    }

    public String[] getFcmTokens() {
        return fcmTokens;
    }

    public void setFcmTokens(String[] fcmTokens) {
        this.fcmTokens = fcmTokens;
    }
}
