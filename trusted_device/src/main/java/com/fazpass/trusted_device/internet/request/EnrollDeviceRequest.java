package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EnrollDeviceRequest {

    public EnrollDeviceRequest(String name, String email, String phone, String idCard, String address, String pin, String device, String packageName, boolean trusted, boolean useFingerPrint, boolean usePin, boolean useVpn, String notificationToken, String meta, String key, String timeZone, int contacts, Location location, List<Sim> sims) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.idCard = idCard;
        this.address = address;
        this.pin = pin;
        this.device = device;
        this.packageName = packageName;
        this.trusted = trusted;
        this.useFingerPrint = useFingerPrint;
        this.usePin = usePin;
        this.useVpn = useVpn;
        this.notificationToken = notificationToken;
        this.meta = meta;
        this.key = key;
        this.timeZone = timeZone;
        this.contacts = contacts;
        this.location = location;
        this.sims = sims;
    }

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("ktp")
    private String idCard;

    @SerializedName("address")
    private String address;

    @SerializedName("pin")
    private String pin;

    @SerializedName("device")
    private String device;

    @SerializedName("app")
    private String packageName;

    @SerializedName("is_trusted")
    private boolean trusted;

    @SerializedName("use_fingerprint")
    private boolean useFingerPrint;

    @SerializedName("use_pin")
    private boolean usePin;

    @SerializedName("is_vpn")
    private boolean useVpn;

    @SerializedName("notification_token")
    private String notificationToken;

    @SerializedName("meta")
    private String meta;

    @SerializedName("key")
    private String key;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("contact_count")
    private int contacts;

    @SerializedName("location")
    private Location location;

    @SerializedName("sim")
    private List<Sim> sims;

    public String getPin() {
        return pin;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public String getAddress() {
        return address;
    }

    public String getDevice() {
        return device;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public boolean isUseFingerPrint() {
        return useFingerPrint;
    }

    public boolean isUsePin() {
        return usePin;
    }

    public boolean isUseVpn() {
        return useVpn;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public String getMeta() {
        return meta;
    }

    public String getKey() {
        return key;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public int getContacts() {
        return contacts;
    }

    public Location getLocation() {
        return location;
    }

    public List<Sim> getSims() {
        return sims;
    }

    public static class Sim{
        @SerializedName("serial")
        private String serialNumber;
        @SerializedName("phone")
        private String phoneNumber;

        public Sim(String serialNumber, String phoneNumber) {
            this.serialNumber = serialNumber;
            this.phoneNumber = phoneNumber;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }


    //Removed (See ADR TD 0.5)
   /* public static class Contact{
        @SerializedName("name")
        private String name;
        @SerializedName("phone")
        private String phoneNumber;

        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }*/
/*
    public static class Contact {
        @SerializedName("name")
        private String name;
        @SerializedName("phone")
        private List<String> phoneNumber;

        public Contact(String name, List<String> phoneNumber) {

            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public List<String> getPhoneNumber() {
            return phoneNumber;
        }
    }
*/

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




