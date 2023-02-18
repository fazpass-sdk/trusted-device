package com.fazpass.trusted_device;

import android.os.Bundle;

import java.util.Map;

class Notification {
    static final String CROSS_DEVICE_CHANNEL = "cross_device_channel";
    static final String CD_NOTIFICATION_CHANNEL = "cross_device_notification_channel";

    static final String NOTIFICATION_APP = "app";
    static final String NOTIFICATION_DEVICE = "device";
    static final String NOTIFICATION_STATUS = "status";
    static final String NOTIFICATION_ID = "uuid_notif";
    static final String NOTIFICATION_TOKEN = "notification_token";
    static final int NOTIFICATION_REQ_ID = 201;
    static boolean IS_REQUIRE_PIN = false;

    private final String app;
    private final String status;
    private final String device;
    private final String notificationToken;
    private final String notificationId;

    Notification(Bundle extras) {
        app = extras.getString(NOTIFICATION_APP);
        status = extras.getString(NOTIFICATION_STATUS);
        device = extras.getString(NOTIFICATION_DEVICE);
        notificationToken = extras.getString(NOTIFICATION_TOKEN);
        notificationId = extras.getString(NOTIFICATION_ID);
    }

    Notification(Map<String, String> data) {
        app = data.get(NOTIFICATION_APP);
        status = data.get(NOTIFICATION_STATUS);
        device = data.get(NOTIFICATION_DEVICE);
        notificationToken = data.get(NOTIFICATION_TOKEN);
        notificationId = data.get(NOTIFICATION_ID);
    }

    public Bundle toExstras() {
        Bundle bundle = new Bundle();
        bundle.putString(NOTIFICATION_APP, app);
        bundle.putString(NOTIFICATION_STATUS, status);
        bundle.putString(NOTIFICATION_DEVICE, device);
        bundle.putString(NOTIFICATION_TOKEN, notificationToken);
        bundle.putString(NOTIFICATION_ID, notificationId);
        return bundle;
    }

    public String getApp() {
        return app;
    }

    public String getStatus() {
        return status;
    }

    public String getDevice() {
        return device;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public String getNotificationId() {
        return notificationId;
    }
}
