package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class UpdateNotificationRequest {
    @SerializedName("uuid_notif")
    private String notificationId;

    @SerializedName("user_id")
    private String userId;

    public UpdateNotificationRequest(String notificationId, String userId) {
        this.notificationId = notificationId;
        this.userId = userId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }
}
