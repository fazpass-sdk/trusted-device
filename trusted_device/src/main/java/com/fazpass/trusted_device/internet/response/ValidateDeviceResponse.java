package com.fazpass.trusted_device.internet.response;

import com.google.gson.annotations.SerializedName;

public class ValidateDeviceResponse {

    @SerializedName("meta")
    private double meta;

    @SerializedName("key")
    private double key;

    @SerializedName("sim")
    private double sim;

    @SerializedName("contact")
    private double contact;

    @SerializedName("location")
    private double location;

    public ValidateDeviceResponse(double meta, double key, double sim, double contact, double location) {
        this.meta = meta;
        this.key = key;
        this.sim = sim;
        this.contact = contact;
        this.location = location;
    }

    public double getMeta() {
        return meta;
    }

    public double getKey() {
        return key;
    }

    public double getSim() {
        return sim;
    }

    public double getContact() {
        return contact;
    }

    public double getLocation() {
        return location;
    }
}
