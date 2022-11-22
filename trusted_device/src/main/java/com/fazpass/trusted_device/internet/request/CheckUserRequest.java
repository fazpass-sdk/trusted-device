package com.fazpass.trusted_device.internet.request;


import com.google.gson.annotations.SerializedName;

public class CheckUserRequest {
    public CheckUserRequest(String email, String phone, String app, String device, String timezone, Location location) {
        this.email = email;
        this.phone = phone;
        this.app = app;
        this.device = device;
        this.timezone = timezone;
        this.location = location;
    }

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    // This is package name
    @SerializedName("app")
    private String app;

    // This is device meta + device id
    @SerializedName("device")
    private String device;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("location")
    private Location location;

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getApp() {
        return app;
    }

    public String getDevice() {
        return device;
    }

    public String getTimezone() {
        return timezone;
    }

    public Location getLocation() {
        return location;
    }

    public static class Location {
        @SerializedName("lat")
        private String latitude;
        @SerializedName("lng")
        private String longitude;

        public Location(double latitude, double longitude) {
            this.latitude = String.valueOf(latitude);
            this.longitude = String.valueOf(longitude);
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }
    }
}
