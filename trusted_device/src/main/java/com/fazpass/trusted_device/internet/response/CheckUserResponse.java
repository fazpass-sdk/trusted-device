package com.fazpass.trusted_device.internet.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheckUserResponse {
    public CheckUserResponse(User user, Apps apps) {
        this.user = user;
        this.apps = apps;
    }

    @SerializedName("user")
    private User user;

    @SerializedName("apps")
    private Apps apps;

    public User getUser() {
        return user;
    }

    public Apps getApps() {
        return apps;
    }

    public class Apps{
        @SerializedName("current")
        private Current current;
        @SerializedName("others")
        private List<App> others;

        @SerializedName("cross_app")
        private boolean crossApp;

        public Apps(Current current, List<App> others, boolean crossApp) {
            this.current = current;
            this.others = others;
            this.crossApp = crossApp;
        }

        public Current getCurrent() {
            return current;
        }

        public List<App> getOthers() {
            return others;
        }

        public boolean isCrossApp() {
            return crossApp;
        }
    }

    public static class Current{

        public Current(String meta, String key, boolean trusted, boolean use_fingerprint, boolean use_pin, String device) {
            this.meta = meta;
            this.key = key;
            this.trusted = trusted;
            this.use_fingerprint = use_fingerprint;
            this.use_pin = use_pin;
            this.device = device;
        }

        //Hashed information
        @SerializedName("meta")
        private String meta;

        //16 character string for unlock meta
        @SerializedName("key")
        private String key;

        @SerializedName("is_trusted")
        private boolean trusted;

        @SerializedName("use_fingerprint")
        private boolean use_fingerprint;

        @SerializedName("use_pin")
        private boolean use_pin;

        @SerializedName("device")
        private String device;

        public String getMeta() {
            return meta;
        }

        public String getKey() {
            return key;
        }

        public boolean isTrusted() {
            return trusted;
        }

        public boolean isUse_fingerprint() {
            return use_fingerprint;
        }

        public boolean isUse_pin() {
            return use_pin;
        }

        public String getDevice() {
            return device;
        }
    }

    public static class User{

        public User(String id) {
            this.id = id;
        }
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }

    }

    public static class App{
        public App(String app, String meta, String key, boolean trusted, boolean use_fingerprint, boolean use_pin, String device) {
            this.meta = meta;
            this.key = key;
            this.trusted = trusted;
            this.use_fingerprint = use_fingerprint;
            this.use_pin = use_pin;
            this.device = device;
        }

        //Package name
        @SerializedName("app")
        private String app;

        //Hashed information
        @SerializedName("meta")
        private String meta;

        //16 character string for unlock meta
        @SerializedName("key")
        private String key;

        @SerializedName("is_trusted")
        private boolean trusted;

        @SerializedName("use_fingerprint")
        private boolean use_fingerprint;

        @SerializedName("use_pin")
        private boolean use_pin;

        @SerializedName("device")
        private String device;

        public String getApp() {
            return app;
        }

        public String getMeta() {
            return meta;
        }

        public String getKey() {
            return key;
        }

        public boolean isTrusted() {
            return trusted;
        }

        public boolean isUse_fingerprint() {
            return use_fingerprint;
        }

        public boolean isUse_pin() {
            return use_pin;
        }

        public String getDevice() {
            return device;
        }
    }

}



