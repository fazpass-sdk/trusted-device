package com.fazpass.trusted_device;

public class OtpResponse {
    private final boolean status;
    private final String message;
    private final String otpId;
    private final int otpLength;

    public OtpResponse(boolean status, String message, String otpId, int otpLength) {
        this.status = status;
        this.message = message;
        this.otpId = otpId;
        this.otpLength = otpLength;
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

    public int getOtpLength() {
        return otpLength;
    }
}