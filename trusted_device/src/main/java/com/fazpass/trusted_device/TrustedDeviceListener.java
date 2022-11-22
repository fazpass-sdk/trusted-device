package com.fazpass.trusted_device;

public interface TrustedDeviceListener<T> {
    void onSuccess(T o);
    void onFailure(Throwable err);
}
