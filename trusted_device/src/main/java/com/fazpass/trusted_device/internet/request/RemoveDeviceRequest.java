package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class RemoveDeviceRequest {
    public RemoveDeviceRequest(String userId, String packageName, String device, Location location, String timeZone) {
        this.userId = userId;
        this.packageName = packageName;
        this.device = device;
        this.location = location;
        this.timeZone = timeZone;
    }

    @SerializedName("user_id")
    private String userId;

    @SerializedName("app")
    private String packageName;

    @SerializedName("device")
    private String device;

    @SerializedName("location")
    private Location location;

    @SerializedName("time_zone")
    private String timeZone;

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
