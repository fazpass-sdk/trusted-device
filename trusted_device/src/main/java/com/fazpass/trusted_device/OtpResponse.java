package com.fazpass.trusted_device;

import com.google.gson.annotations.SerializedName;

public class OtpResponse {
    private final boolean status;
    private final String message;
    private final String otpId;

    public OtpResponse(boolean status, String message, String otpId) {
        this.status = status;
        this.message = message;
        this.otpId = otpId;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getOtpId() {
        return otpId;
    }
}