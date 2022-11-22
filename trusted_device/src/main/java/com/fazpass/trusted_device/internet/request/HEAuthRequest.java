package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class HEAuthRequest {

    @SerializedName("gateway_key")
    private String gatewayKey;

    @SerializedName("phone_number")
    private String phoneNumber;

    public HEAuthRequest(String gatewayKey, String phoneNumber) {
        this.gatewayKey = gatewayKey;
        this.phoneNumber = phoneNumber;
    }

    public String getGatewayKey() {
        return gatewayKey;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
