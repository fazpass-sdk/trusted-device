package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OTPWithPhoneRequest {

    @SerializedName("phone")
    private String phone;

    @SerializedName("gateway_key")
    private String gatewayKey;

    @SerializedName("params")
    private List<Param> params;

    public OTPWithPhoneRequest(String phone, String gatewayKey, List<Param> params) {
        this.phone = phone;
        this.gatewayKey = gatewayKey;
        this.params = params;
    }

    public String getPhone() {
        return phone;
    }

    public String getGatewayKey() {
        return gatewayKey;
    }

    public List<Param> getParams() {
        return params;
    }

    public static class Param {

        @SerializedName("tag")
        private String tag;

        @SerializedName("value")
        private String value;

        public Param(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public String getTag() {
            return tag;
        }

        public String getValue() {
            return value;
        }
    }
}
