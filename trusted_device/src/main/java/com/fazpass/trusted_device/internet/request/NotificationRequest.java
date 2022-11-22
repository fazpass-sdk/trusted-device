package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationRequest {
    public NotificationRequest(long timeOut, String notificationToken, String notificationId, String userId, List<Device> devices, String thisDevice) {
        this.timeOut = timeOut;
        this.notificationToken = notificationToken;
        this.notificationId = notificationId;
        this.userId = userId;
        this.devices = devices;
        this.thisDevice = thisDevice;
    }

    @SerializedName("count_expired")
    private long timeOut;

    @SerializedName("notification_token")
    private String notificationToken;

    @SerializedName("uuid_notif")
    private String notificationId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("other_device")
    private List<Device> devices;

    @SerializedName("device")
    private String thisDevice;

    public long getTimeOut() {
        return timeOut;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public String getThisDevice() {
        return thisDevice;
    }

    public static class Device{

        @SerializedName("app")
        private String app;

        @SerializedName("device")
        private String device;

        public Device(String app, String device) {
            this.app = app;
            this.device = device;
        }

        public String getApp() {
            return app;
        }

        public String getDevice() {
            return device;
        }
    }
}
