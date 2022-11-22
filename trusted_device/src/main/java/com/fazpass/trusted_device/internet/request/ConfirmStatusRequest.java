package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class ConfirmStatusRequest {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("uuid_notif")
    private String uuId;

    @SerializedName("device")
    private String device;

    @SerializedName("app")
    private String packageName;

    @SerializedName("notification_token")
    private String notificationToken;

    @SerializedName("is_confirmation")
    private String isConfirmation;

    public ConfirmStatusRequest(String userId, String uuId, String device, String packageName, String notificationToken, String isConfirmation) {
        this.userId = userId;
        this.uuId = uuId;
        this.device = device;
        this.packageName = packageName;
        this.notificationToken = notificationToken;
        this.isConfirmation = isConfirmation;
    }

    public String getUserId() {
        return userId;
    }

    public String getUuId() {
        return uuId;
    }

    public String getDevice() {
        return device;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public String getIsConfirmation() {
        return isConfirmation;
    }
}
