package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class ValidatePinRequest {

    @SerializedName("pin")
    private String pin;

    @SerializedName("app")
    private String packageName;

    @SerializedName("device")
    private String deviceName;

    @SerializedName("user_id")
    private String userId;

    public ValidatePinRequest(String pin, String packageName, String deviceName, String userId) {
        this.pin = pin;
        this.packageName = packageName;
        this.deviceName = deviceName;
        this.userId = userId;
    }

    public String getPin() {
        return pin;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getUserId() {
        return userId;
    }
}
