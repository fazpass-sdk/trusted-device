package com.fazpass.trusted_device;

public interface CrossDeviceListener {
    void onResponse(String device, boolean status);
    void onExpired();
}
