package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class UpdateFcmRequest {

    public UpdateFcmRequest(String userId, String packageName, String device, String fcm, String key) {
        this.userId = userId;
        this.packageName = packageName;
        this.device = device;
        this.fcm = fcm;
        this.key = key;
    }

    @SerializedName("user_id")
    private String userId;

    @SerializedName("app")
    private String packageName;

    @SerializedName("device")
    private String device;

    @SerializedName("notification_token")
    private String fcm;

    @SerializedName("key")
    private String key;

    public String getUserId() {
        return userId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDevice() {
        return device;
    }

    public String getFcm() {
        return fcm;
    }

    public String getKey() {
        return key;
    }
}
