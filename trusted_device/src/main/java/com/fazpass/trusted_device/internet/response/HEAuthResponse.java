package com.fazpass.trusted_device.internet.response;

import com.google.gson.annotations.SerializedName;

public class HEAuthResponse {

    @SerializedName("authpage")
    private String authPage;

    public HEAuthResponse(String authPage) {
        this.authPage = authPage;
    }

    public String getAuthPage() {
        return authPage;
    }
}
