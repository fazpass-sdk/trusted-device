package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class LogFraudRequest {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("device")
    private String device;

    @SerializedName("app")
    private String packageName;

    private int reason;

    public LogFraudRequest(String userId, String device, String packageName, int reason) {
        this.userId = userId;
        this.device = device;
        this.packageName = packageName;
        this.reason = reason;
    }

    public String getUserId() {
        return userId;
    }

    public String getDevice() {
        return device;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }
}
